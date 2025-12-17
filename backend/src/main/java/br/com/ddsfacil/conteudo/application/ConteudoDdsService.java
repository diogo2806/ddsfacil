package br.com.ddsfacil.conteudo.application;

import br.com.ddsfacil.configuracao.multitenant.ContextoEmpresa;
import br.com.ddsfacil.conteudo.domain.ConteudoDdsEntity;
import br.com.ddsfacil.conteudo.domain.TipoConteudo;
import br.com.ddsfacil.conteudo.infrastructure.ConteudoDdsRepository;
import br.com.ddsfacil.conteudo.infrastructure.dto.ConteudoDdsArquivoResponse;
import br.com.ddsfacil.conteudo.infrastructure.dto.ConteudoDdsRequest;
import br.com.ddsfacil.conteudo.infrastructure.dto.ConteudoDdsResponse;
import br.com.ddsfacil.envio.infrastructure.EnvioDdsRepository; // <--- IMPORTAR
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.excecao.RegraNegocioException; // <--- IMPORTAR
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ConteudoDdsService {

    private static final Logger log = LoggerFactory.getLogger(ConteudoDdsService.class);
    
    private final ConteudoDdsRepository conteudoRepositorio;
    private final EnvioDdsRepository envioRepositorio; // <--- ADICIONAR DEPENDENCIA

    // ATUALIZAR CONSTRUTOR
    public ConteudoDdsService(ConteudoDdsRepository conteudoRepositorio, EnvioDdsRepository envioRepositorio) {
        this.conteudoRepositorio = conteudoRepositorio;
        this.envioRepositorio = envioRepositorio;
    }

    @Transactional
    public ConteudoDdsResponse criar(ConteudoDdsRequest requisicao) {
        // ... (código existente mantido igual) ...
        Objects.requireNonNull(requisicao, "Requisição não pode ser nula.");
        String tituloLimpo = sanitizarTextoCurto(requisicao.getTitulo());
        String descricaoLimpa = sanitizarTextoLongo(requisicao.getDescricao());

        String tipoString = requisicao.getTipo() == null ? "TEXTO" : requisicao.getTipo();
        TipoConteudo tipoEnum = TipoConteudo.fromString(tipoString);

        String url = requisicao.getUrl();
        String arquivoNome = requisicao.getArquivoNome();
        byte[] arquivoDados = null;

        if (tipoEnum == TipoConteudo.ARQUIVO) {
            MultipartFile arquivo = requisicao.getArquivo();
            if (arquivo == null || arquivo.isEmpty()) {
                throw new IllegalArgumentException("O arquivo é obrigatório para conteúdos do tipo ARQUIVO.");
            }

            String nomeLimpo = StringUtils.cleanPath(Objects.requireNonNull(arquivo.getOriginalFilename(), "Nome do arquivo inválido."));
            if (nomeLimpo.contains("..")) {
                throw new IllegalArgumentException("Nome de arquivo inválido.");
            }

            try {
                arquivoDados = arquivo.getBytes();
                arquivoNome = nomeLimpo;
                url = null;
            } catch (Exception e) {
                log.error("Erro ao processar o arquivo enviado.", e);
                throw new IllegalStateException("Não foi possível processar o arquivo enviado.", e);
            }
        }

        log.info("Criando novo conteúdo. Título: {}, Tipo: {}", tituloLimpo, tipoEnum);
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();

        ConteudoDdsEntity conteudo = new ConteudoDdsEntity(
                tituloLimpo,
                descricaoLimpa,
                tipoEnum,
                url,
                arquivoNome,
                null,
                arquivoDados,
                empresaId
        );
        ConteudoDdsEntity salvo = conteudoRepositorio.save(conteudo);
        log.info("Conteúdo criado com ID: {}", salvo.getId());
        return mapearParaResposta(salvo);
    }

    @Transactional(readOnly = true)
    public List<ConteudoDdsResponse> listarTodos() {
        return conteudoRepositorio.findAll().stream()
                .map(this::mapearParaResposta)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConteudoDdsArquivoResponse buscarArquivo(Long id) {
         // ... (código existente mantido igual) ...
        ConteudoDdsEntity conteudo = conteudoRepositorio
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo não encontrado."));

        if (conteudo.getTipo() != TipoConteudo.ARQUIVO) {
            throw new RecursoNaoEncontradoException("Nenhum arquivo disponível para este conteúdo.");
        }

        byte[] dadosArquivo = conteudo.getArquivoDados();
        if (dadosArquivo == null || dadosArquivo.length == 0) {
            throw new RecursoNaoEncontradoException("Arquivo do conteúdo não encontrado ou vazio.");
        }

        String nomeArquivo = conteudo.getArquivoNome() == null ? "arquivo-dds" : conteudo.getArquivoNome();
        MediaType tipoMidia = MediaTypeFactory
                .getMediaType(nomeArquivo)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return new ConteudoDdsArquivoResponse(nomeArquivo, tipoMidia, dadosArquivo);
    }

    @Transactional
    public void remover(Long id) {
        log.info("Tentando remover conteúdo ID: {}", id);
        
        if (!conteudoRepositorio.existsById(id)) {
            log.warn("Conteúdo ID: {} não encontrado para remoção.", id);
            throw new RecursoNaoEncontradoException("Conteúdo não encontrado.");
        }

        // --- NOVA VALIDAÇÃO ---
        if (envioRepositorio.existsByConteudoId(id)) {
            log.warn("Tentativa de remover conteúdo ID: {} que possui envios vinculados.", id);
            throw new RegraNegocioException("Não é possível excluir este conteúdo pois ele já foi enviado para funcionários. Para manter o histórico de segurança, o conteúdo não pode ser apagado.");
        }
        // ----------------------

        conteudoRepositorio.deleteById(id);
        log.info("Conteúdo ID: {} removido com sucesso.", id);
    }

    private ConteudoDdsResponse mapearParaResposta(ConteudoDdsEntity conteudo) {
        return new ConteudoDdsResponse(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getDescricao(),
                conteudo.getTipo().getDescricao(),
                conteudo.getUrl(),
                conteudo.getArquivoNome(),
                conteudo.getArquivoDados()
        );
    }
    
    // ... (métodos privados auxiliares mantidos iguais) ...
    private String sanitizarTextoCurto(String texto) {
        return Jsoup.clean(texto, Safelist.none());
    }

    private String sanitizarTextoLongo(String texto) {
        Safelist permissoesBasicas = Safelist.none();
        return Jsoup.clean(texto, permissoesBasicas);
    }
}
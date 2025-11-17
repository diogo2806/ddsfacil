// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/ConteudoDdsService.java
package br.com.ddsfacil.conteudo;

import br.com.ddsfacil.conteudo.dto.ConteudoDdsRequest;
import br.com.ddsfacil.conteudo.dto.ConteudoDdsResponse;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ConteudoDdsService {

    private static final Logger log = LoggerFactory.getLogger(ConteudoDdsService.class);
    private final ConteudoDdsRepository conteudoRepositorio;

    public ConteudoDdsService(ConteudoDdsRepository conteudoRepositorio) {
        this.conteudoRepositorio = conteudoRepositorio;
    }

    @Transactional
    public ConteudoDdsResponse criar(ConteudoDdsRequest requisicao) {
        Objects.requireNonNull(requisicao, "Requisição não pode ser nula.");

        String tituloLimpo = sanitizarTextoCurto(requisicao.getTitulo());
        String descricaoLimpa = sanitizarTextoLongo(requisicao.getDescricao());

        // Converte a String do DTO para o Enum
        String tipoString = requisicao.getTipo() == null ? "TEXTO" : requisicao.getTipo();
        TipoConteudo tipoEnum = TipoConteudo.fromString(tipoString);

        String url = requisicao.getUrl();
        String arquivoNome = requisicao.getArquivoNome();
        String arquivoPath = null;
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
                arquivoPath = null;
            } catch (Exception e) {
                log.error("Erro ao processar o arquivo enviado.", e);
                throw new IllegalStateException("Não foi possível processar o arquivo enviado.", e);
            }
        }

        log.info("Criando novo conteúdo. Título: {}, Tipo: {}", tituloLimpo, tipoEnum);

        ConteudoDdsEntity conteudo = new ConteudoDdsEntity(tituloLimpo, descricaoLimpa, tipoEnum, url, arquivoNome, arquivoPath, arquivoDados);
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

    @Transactional
    public void remover(Long id) {
        log.info("Tentando remover conteúdo ID: {}", id);
        if (!conteudoRepositorio.existsById(id)) {
            log.warn("Conteúdo ID: {} não encontrado para remoção.", id);
            throw new RecursoNaoEncontradoException("Conteúdo não encontrado.");
        }
        conteudoRepositorio.deleteById(id);
        log.info("Conteúdo ID: {} removido com sucesso.", id);
    }

    private ConteudoDdsResponse mapearParaResposta(ConteudoDdsEntity conteudo) {
        return new ConteudoDdsResponse(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getDescricao(),
                conteudo.getTipo().getDescricao(), // Retorna a descrição do Enum
                conteudo.getUrl(),
                conteudo.getArquivoNome(),
                conteudo.getArquivoPath(),
                conteudo.getArquivoDados()
        );
    }

    private String sanitizarTextoCurto(String texto) {
        return Jsoup.clean(texto, Safelist.none());
    }

    private String sanitizarTextoLongo(String texto) {
        Safelist permissoesBasicas = Safelist.none();
        return Jsoup.clean(texto, permissoesBasicas);
    }
}
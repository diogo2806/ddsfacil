// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/ConteudoDdsServico.java
package br.com.ddsfacil.conteudo;

import br.com.ddsfacil.conteudo.dto.ConteudoDdsRequisicao;
import br.com.ddsfacil.conteudo.dto.ConteudoDdsResposta;
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

@Service
public class ConteudoDdsServico {

    private static final Logger log = LoggerFactory.getLogger(ConteudoDdsServico.class);
    private final ConteudoDdsRepositorio conteudoRepositorio;

    public ConteudoDdsServico(ConteudoDdsRepositorio conteudoRepositorio) {
        this.conteudoRepositorio = conteudoRepositorio;
    }

    @Transactional
    public ConteudoDdsResposta criar(ConteudoDdsRequisicao requisicao) {
        Objects.requireNonNull(requisicao, "Requisição não pode ser nula.");

        String tituloLimpo = sanitizarTextoCurto(requisicao.getTitulo());
        String descricaoLimpa = sanitizarTextoLongo(requisicao.getDescricao());

        // Converte a String do DTO para o Enum
        String tipoString = requisicao.getTipo() == null ? "TEXTO" : requisicao.getTipo();
        TipoConteudo tipoEnum = TipoConteudo.fromString(tipoString);

        String url = requisicao.getUrl();
        String arquivoNome = requisicao.getArquivoNome();
        String arquivoPath = null; // O path é salvo apenas no upload real, aqui usamos a URL

        log.info("Criando novo conteúdo. Título: {}, Tipo: {}", tituloLimpo, tipoEnum);

        ConteudoDds conteudo = new ConteudoDds(tituloLimpo, descricaoLimpa, tipoEnum, url, arquivoNome, arquivoPath);
        ConteudoDds salvo = conteudoRepositorio.save(conteudo);
        log.info("Conteúdo criado com ID: {}", salvo.getId());
        return mapearParaResposta(salvo);
    }

    @Transactional(readOnly = true)
    public List<ConteudoDdsResposta> listarTodos() {
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

    private ConteudoDdsResposta mapearParaResposta(ConteudoDds conteudo) {
        return new ConteudoDdsResposta(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getDescricao(),
                conteudo.getTipo().getDescricao(), // Retorna a descrição do Enum
                conteudo.getUrl(),
                conteudo.getArquivoNome(),
                conteudo.getArquivoPath()
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
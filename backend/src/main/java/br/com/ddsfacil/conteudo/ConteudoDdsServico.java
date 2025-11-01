package br.com.ddsfacil.conteudo;

import br.com.ddsfacil.conteudo.dto.ConteudoDdsRequisicao;
import br.com.ddsfacil.conteudo.dto.ConteudoDdsResposta;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConteudoDdsServico {

    private final ConteudoDdsRepositorio conteudoRepositorio;

    public ConteudoDdsServico(ConteudoDdsRepositorio conteudoRepositorio) {
        this.conteudoRepositorio = conteudoRepositorio;
    }

    @Transactional
    public ConteudoDdsResposta criar(ConteudoDdsRequisicao requisicao) {
        String tituloLimpo = sanitizarTextoCurto(requisicao.getTitulo());
        String descricaoLimpa = sanitizarTextoLongo(requisicao.getDescricao());

        ConteudoDds conteudo = new ConteudoDds(tituloLimpo, descricaoLimpa);
        ConteudoDds salvo = conteudoRepositorio.save(conteudo);
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
        conteudoRepositorio.deleteById(id);
    }

    private ConteudoDdsResposta mapearParaResposta(ConteudoDds conteudo) {
        return new ConteudoDdsResposta(conteudo.getId(), conteudo.getTitulo(), conteudo.getDescricao());
    }

    private String sanitizarTextoCurto(String texto) {
        return Jsoup.clean(texto, Safelist.none());
    }

    private String sanitizarTextoLongo(String texto) {
        Safelist permissoesBasicas = Safelist.none();
        return Jsoup.clean(texto, permissoesBasicas);
    }
}

package br.com.ddsfacil.confirmacaoTrabalhador;

import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorConfirmacaoResposta;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorResposta;
import br.com.ddsfacil.envio.EnvioDds;
import br.com.ddsfacil.envio.EnvioDdsRepositorio;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import java.time.LocalDateTime;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmacaoTrabalhadorServico {

    private final EnvioDdsRepositorio envioRepositorio;

    public ConfirmacaoTrabalhadorServico(EnvioDdsRepositorio envioRepositorio) {
        this.envioRepositorio = envioRepositorio;
    }

    @Transactional(readOnly = true)
    public ConfirmacaoTrabalhadorResposta buscarPorToken(String tokenAcesso) {
        EnvioDds envio = localizarPorToken(tokenAcesso);
        String titulo = Jsoup.clean(envio.getConteudo().getTitulo(), Safelist.none()).strip();
        String descricao = Jsoup.clean(envio.getConteudo().getDescricao(), Safelist.none()).strip();
        return new ConfirmacaoTrabalhadorResposta(titulo, descricao);
    }

    @Transactional
    public ConfirmacaoTrabalhadorConfirmacaoResposta confirmarPorToken(String tokenAcesso, LocalDateTime momento) {
        EnvioDds envio = localizarPorToken(tokenAcesso);
        envio.confirmar(momento);
        return new ConfirmacaoTrabalhadorConfirmacaoResposta(envio.getStatus(), envio.getMomentoConfirmacao());
    }

    private EnvioDds localizarPorToken(String tokenAcesso) {
        String tokenSanitizado = Jsoup.clean(tokenAcesso == null ? "" : tokenAcesso, Safelist.none()).trim();
        if (tokenSanitizado.isEmpty()) {
            throw new RecursoNaoEncontradoException("Envio não encontrado para o token informado.");
        }
        return envioRepositorio
            .findByTokenAcesso(tokenSanitizado)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Envio não encontrado para o token informado."));
    }
}

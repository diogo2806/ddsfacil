package br.com.ddsfacil.confirmacaoTrabalhador;

import br.com.ddsfacil.conteudo.ConteudoDdsEntity;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorConfirmacaoResponse;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorResponse;
import br.com.ddsfacil.conteudo.TipoConteudo;
import br.com.ddsfacil.envio.EnvioDdsEntity;
import br.com.ddsfacil.envio.EnvioDdsRepository;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import java.time.LocalDateTime;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmacaoTrabalhadorService {

    private final EnvioDdsRepository envioRepositorio;

    public ConfirmacaoTrabalhadorService(EnvioDdsRepository envioRepositorio) {
        this.envioRepositorio = envioRepositorio;
    }

    @Transactional(readOnly = true)
    public ConfirmacaoTrabalhadorResponse buscarPorToken(String tokenAcesso) {
        EnvioDdsEntity envio = localizarPorToken(tokenAcesso);
        String titulo = Jsoup.clean(envio.getConteudo().getTitulo(), Safelist.none()).strip();
        String descricao = Jsoup.clean(envio.getConteudo().getDescricao(), Safelist.none()).strip();
        TipoConteudo tipoConteudo = envio.getConteudo().getTipo();
        String urlConteudo = resolverUrlConteudo(envio.getConteudo());
        String nomeArquivo = limparCampoOpcional(envio.getConteudo().getArquivoNome());

        return new ConfirmacaoTrabalhadorResponse(titulo, descricao, tipoConteudo, urlConteudo, nomeArquivo);
    }

    @Transactional
    public ConfirmacaoTrabalhadorConfirmacaoResponse confirmarPorToken(String tokenAcesso, LocalDateTime momento) {
        EnvioDdsEntity envio = localizarPorToken(tokenAcesso);
        envio.confirmar(momento);
        return new ConfirmacaoTrabalhadorConfirmacaoResponse(envio.getStatus(), envio.getMomentoConfirmacao());
    }

    private EnvioDdsEntity localizarPorToken(String tokenAcesso) {
        String tokenSanitizado = Jsoup.clean(tokenAcesso == null ? "" : tokenAcesso, Safelist.none()).trim();
        if (tokenSanitizado.isEmpty()) {
            throw new RecursoNaoEncontradoException("Envio não encontrado para o token informado.");
        }
        return envioRepositorio
            .findByTokenAcesso(tokenSanitizado)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Envio não encontrado para o token informado."));
    }

    private String resolverUrlConteudo(ConteudoDdsEntity conteudo) {
        String urlLimpa = limparCampoOpcional(conteudo.getUrl());
        if (conteudo.getTipo() == TipoConteudo.ARQUIVO) {
            String caminhoArquivoLimpo = limparCampoOpcional(conteudo.getArquivoPath());
            if (caminhoArquivoLimpo != null) {
                return caminhoArquivoLimpo;
            }
        }
        return urlLimpa;
    }

    private String limparCampoOpcional(String valor) {
        String valorLimpo = Jsoup.clean(valor == null ? "" : valor, Safelist.none()).trim();
        return valorLimpo.isEmpty() ? null : valorLimpo;
    }
}

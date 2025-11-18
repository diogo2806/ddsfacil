package br.com.ddsfacil.confirmacaoTrabalhador;

import br.com.ddsfacil.conteudo.ConteudoDdsEntity;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorArquivoResponse;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorConfirmacaoResponse;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorResponse;
import br.com.ddsfacil.conteudo.TipoConteudo;
import br.com.ddsfacil.envio.EnvioDdsEntity;
import br.com.ddsfacil.envio.EnvioDdsRepository;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import java.time.LocalDateTime;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
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
        String tokenSanitizado = sanitizarToken(tokenAcesso);
        EnvioDdsEntity envio = localizarPorToken(tokenSanitizado);
        String titulo = Jsoup.clean(envio.getConteudo().getTitulo(), Safelist.none()).strip();
        String descricao = Jsoup.clean(envio.getConteudo().getDescricao(), Safelist.none()).strip();
        TipoConteudo tipoConteudo = envio.getConteudo().getTipo();
        String urlConteudo = resolverUrlConteudo(envio.getConteudo(), tokenSanitizado);
        String nomeArquivo = limparCampoOpcional(envio.getConteudo().getArquivoNome());

        return new ConfirmacaoTrabalhadorResponse(titulo, descricao, tipoConteudo, urlConteudo, nomeArquivo);
    }

    @Transactional
    public ConfirmacaoTrabalhadorConfirmacaoResponse confirmarPorToken(String tokenAcesso, LocalDateTime momento) {
        EnvioDdsEntity envio = localizarPorToken(sanitizarToken(tokenAcesso));
        envio.confirmar(momento);
        return new ConfirmacaoTrabalhadorConfirmacaoResponse(envio.getStatus(), envio.getMomentoConfirmacao());
    }

    @Transactional(readOnly = true)
    public ConfirmacaoTrabalhadorArquivoResponse buscarArquivoPorToken(String tokenAcesso) {
        String tokenSanitizado = sanitizarToken(tokenAcesso);
        EnvioDdsEntity envio = localizarPorToken(tokenSanitizado);

        if (envio.getConteudo().getTipo() != TipoConteudo.ARQUIVO) {
            throw new RecursoNaoEncontradoException("Nenhum arquivo disponível para este DDS.");
        }

        byte[] dadosArquivo = obterDadosArquivo(envio.getConteudo());
        if (dadosArquivo == null || dadosArquivo.length == 0) {
            throw new RecursoNaoEncontradoException("Arquivo do DDS não encontrado ou vazio.");
        }

        String nomeArquivoSeguro = limparCampoOpcional(envio.getConteudo().getArquivoNome());
        if (nomeArquivoSeguro == null) {
            nomeArquivoSeguro = "arquivo-dds";
        }

        MediaType tipoMidia = MediaTypeFactory
            .getMediaType(nomeArquivoSeguro)
            .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return new ConfirmacaoTrabalhadorArquivoResponse(nomeArquivoSeguro, tipoMidia, dadosArquivo);
    }

    private EnvioDdsEntity localizarPorToken(String tokenSanitizado) {
        return envioRepositorio
            .findByTokenAcesso(tokenSanitizado)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Envio não encontrado para o token informado."));
    }

    private String sanitizarToken(String tokenAcesso) {
        String tokenSanitizado = Jsoup.clean(tokenAcesso == null ? "" : tokenAcesso, Safelist.none()).trim();
        if (tokenSanitizado.isEmpty()) {
            throw new RecursoNaoEncontradoException("Envio não encontrado para o token informado.");
        }
        return tokenSanitizado;
    }

    private String resolverUrlConteudo(ConteudoDdsEntity conteudo, String tokenSanitizado) {
        String urlLimpa = limparCampoOpcional(conteudo.getUrl());

        if (conteudo.getTipo() == TipoConteudo.ARQUIVO) {
            return "/api/public/dds/" + tokenSanitizado + "/arquivo";
        }

        return urlLimpa;
    }

    private String limparCampoOpcional(String valor) {
        String valorLimpo = Jsoup.clean(valor == null ? "" : valor, Safelist.none()).trim();
        return valorLimpo.isEmpty() ? null : valorLimpo;
    }

    private byte[] obterDadosArquivo(ConteudoDdsEntity conteudo) {
        if (conteudo.getArquivoDados() != null && conteudo.getArquivoDados().length > 0) {
            return conteudo.getArquivoDados();
        }
        return null;
    }
}

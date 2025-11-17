package br.com.ddsfacil.confirmacaoTrabalhador;

import br.com.ddsfacil.conteudo.ConteudoDdsEntity;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorArquivoResponse;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorConfirmacaoResponse;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorResponse;
import br.com.ddsfacil.conteudo.TipoConteudo;
import br.com.ddsfacil.envio.EnvioDdsEntity;
import br.com.ddsfacil.envio.EnvioDdsRepository;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            String caminhoArquivoLimpo = normalizarCaminhoArquivo(conteudo.getArquivoPath());
            if (caminhoArquivoLimpo != null) {
                return caminhoArquivoLimpo;
            }
            return "/api/public/dds/" + tokenSanitizado + "/arquivo";
        }

        return urlLimpa;
    }

    private String limparCampoOpcional(String valor) {
        String valorLimpo = Jsoup.clean(valor == null ? "" : valor, Safelist.none()).trim();
        return valorLimpo.isEmpty() ? null : valorLimpo;
    }

    private String normalizarCaminhoArquivo(String caminho) {
        String caminhoLimpo = limparCampoOpcional(caminho);
        if (caminhoLimpo == null) {
            return null;
        }

        String caminhoNormalizado = caminhoLimpo.replace("\\", "/");
        if (caminhoNormalizado.contains("..")) {
            return null;
        }

        if (caminhoNormalizado.startsWith("/")) {
            return caminhoNormalizado;
        }

        return "/" + caminhoNormalizado;
    }

    private byte[] obterDadosArquivo(ConteudoDdsEntity conteudo) {
        if (conteudo.getArquivoDados() != null && conteudo.getArquivoDados().length > 0) {
            return conteudo.getArquivoDados();
        }

        String caminhoArquivo = normalizarCaminhoArquivo(conteudo.getArquivoPath());
        if (caminhoArquivo == null) {
            return null;
        }

        Path pathArquivo = Paths.get(caminhoArquivo.replaceFirst("^/", ""));
        if (!Files.exists(pathArquivo) || !Files.isRegularFile(pathArquivo)) {
            return null;
        }

        if (!pathArquivo.startsWith(Paths.get("uploads"))) {
            return null;
        }

        try {
            return Files.readAllBytes(pathArquivo);
        } catch (IOException e) {
            return null;
        }
    }
}

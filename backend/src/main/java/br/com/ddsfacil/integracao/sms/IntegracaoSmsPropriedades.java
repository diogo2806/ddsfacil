package br.com.ddsfacil.integracao.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "integracao.sms")
public class IntegracaoSmsPropriedades {

    private boolean habilitado;
    private String contaSid;
    private String tokenAutenticacao;
    private String numeroOrigem;
    private String urlBaseConfirmacao;

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public String getContaSid() {
        return contaSid;
    }

    public void setContaSid(String contaSid) {
        this.contaSid = contaSid;
    }

    public String getTokenAutenticacao() {
        return tokenAutenticacao;
    }

    public void setTokenAutenticacao(String tokenAutenticacao) {
        this.tokenAutenticacao = tokenAutenticacao;
    }

    public String getNumeroOrigem() {
        return numeroOrigem;
    }

    public void setNumeroOrigem(String numeroOrigem) {
        this.numeroOrigem = numeroOrigem;
    }

    public String getUrlBaseConfirmacao() {
        return urlBaseConfirmacao;
    }

    public void setUrlBaseConfirmacao(String urlBaseConfirmacao) {
        this.urlBaseConfirmacao = urlBaseConfirmacao;
    }

    public boolean credenciaisCompletas() {
        return naoVazio(contaSid) && naoVazio(tokenAutenticacao) && naoVazio(numeroOrigem);
    }

    public boolean urlConfirmacaoValida() {
        return naoVazio(urlBaseConfirmacao);
    }

    private boolean naoVazio(String valor) {
        return valor != null && !valor.isBlank();
    }
}

// Arquivo: backend/src/main/java/br/com/ddsfacil/integracao/sms/IntegracaoSmsPropriedades.java
package br.com.ddsfacil.integracao.sms;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConfigurationProperties(prefix = "integracao.sms")
@Getter
@Setter
public class IntegracaoSmsPropriedades {

    private boolean habilitado;
    private String contaSid;
    private String tokenAutenticacao;
    private String numeroOrigem;
    private String urlBaseConfirmacao;
    private String urlStatusWebhook;

    public boolean credenciaisCompletas() {
        return naoVazio(contaSid) && naoVazio(tokenAutenticacao) && naoVazio(numeroOrigem);
    }

    public boolean urlConfirmacaoValida() {
        return naoVazio(urlBaseConfirmacao);
    }

    public boolean urlWebhookValida() {
        return naoVazio(urlStatusWebhook);
    }

    public String montarUrlStatusCallback(Long envioId) {
        if (!urlWebhookValida() || envioId == null) {
            return null;
        }
        return UriComponentsBuilder
                .fromUriString(urlStatusWebhook)
                .replaceQueryParam("envioId", envioId)
                .build()
                .toUriString();
    }

    private boolean naoVazio(String valor) {
        return valor != null && !valor.isBlank();
    }
}
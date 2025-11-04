// Arquivo: backend/src/main/java/br/com/ddsfacil/integracao/sms/IntegracaoSmsPropriedades.java
package br.com.ddsfacil.integracao.sms;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
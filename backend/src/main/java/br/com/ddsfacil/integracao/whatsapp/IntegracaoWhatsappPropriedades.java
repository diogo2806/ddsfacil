package br.com.ddsfacil.integracao.whatsapp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "integracao.whatsapp")
@Getter
@Setter
public class IntegracaoWhatsappPropriedades {

    private boolean habilitado;
    private String contaSid;
    private String tokenAutenticacao;
    private String numeroOrigem;

    public boolean credenciaisCompletas() {
        return naoVazio(contaSid) && naoVazio(tokenAutenticacao) && naoVazio(numeroOrigem);
    }

    private boolean naoVazio(String valor) {
        return valor != null && !valor.isBlank();
    }
}

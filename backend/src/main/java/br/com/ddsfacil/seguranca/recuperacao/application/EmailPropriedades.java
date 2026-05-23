package br.com.ddsfacil.seguranca.recuperacao.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.email")
@Getter
@Setter
public class EmailPropriedades {

    private boolean habilitado;
    private String remetente;
    private String urlBaseRedefinicao;

    public boolean configurado() {
        return habilitado && remetente != null && !remetente.isBlank();
    }
}

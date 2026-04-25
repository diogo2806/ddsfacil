package br.com.ddsfacil.seguranca.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "seguranca.jwt")
@Getter
@Setter
public class TokenJwtPropriedades {

    private String segredo;
    private long validadeMinutos;
    private String emissor = "dds-facil-api";
}

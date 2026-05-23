package br.com.ddsfacil.envio.infrastructure.lembrete;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lembrete")
@Getter
@Setter
public class LembretePropriedades {

    /** Liga/desliga o envio automático de lembretes. */
    private boolean habilitado = true;

    /** Horas mínimas desde o último envio/lembrete antes de cobrar novamente. */
    private int intervaloHoras = 4;

    /** Quantidade máxima de lembretes por envio (além do envio original). */
    private int maximoLembretes = 2;
}

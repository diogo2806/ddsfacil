package br.com.ddsfacil.licenca.infrastructure.dto;

import br.com.ddsfacil.envio.domain.CanalMensagem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para recarga de créditos de mensageria")
@Getter
@Setter
public class RecargaRequest {

    @NotNull(message = "A quantidade de créditos é obrigatória.")
    @Min(value = 1, message = "A quantidade mínima de recarga é 1 crédito.")
    @Max(value = 1000000, message = "Quantidade de recarga acima do permitido.")
    private Integer quantidade;

    @Schema(description = "Canal a recarregar (SMS ou WHATSAPP). Padrão: SMS.", example = "SMS")
    private CanalMensagem canal;
}

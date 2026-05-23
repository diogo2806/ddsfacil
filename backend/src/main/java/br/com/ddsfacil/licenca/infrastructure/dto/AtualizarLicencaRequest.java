package br.com.ddsfacil.licenca.infrastructure.dto;

import br.com.ddsfacil.licenca.domain.StatusPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para atualização do plano e status de pagamento da licença")
@Getter
@Setter
public class AtualizarLicencaRequest {

    @NotBlank(message = "O tipo de plano é obrigatório.")
    @Size(max = 40, message = "O tipo de plano deve ter no máximo 40 caracteres.")
    private String tipoPlano;

    @NotNull(message = "O status de pagamento é obrigatório.")
    private StatusPagamento statusPagamento;

    private LocalDate dataRenovacao;
}

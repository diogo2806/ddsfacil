package br.com.ddsfacil.seguranca.recuperacao.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para solicitar a redefinição de senha por e-mail")
@Getter
@Setter
public class SolicitarRedefinicaoRequest {

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    private String email;
}

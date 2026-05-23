package br.com.ddsfacil.seguranca.recuperacao.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para redefinir a senha usando o token recebido por e-mail")
@Getter
@Setter
public class RedefinirComTokenRequest {

    @NotBlank(message = "O token é obrigatório.")
    private String token;

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 8, max = 100, message = "A nova senha deve ter entre 8 e 100 caracteres.")
    private String novaSenha;
}

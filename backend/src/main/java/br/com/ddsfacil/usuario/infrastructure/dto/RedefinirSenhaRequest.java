package br.com.ddsfacil.usuario.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para redefinição de senha")
@Getter
@Setter
public class RedefinirSenhaRequest {

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.")
    private String novaSenha;
}

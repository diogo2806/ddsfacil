package br.com.ddsfacil.usuario.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para o usuário autenticado alterar a própria senha")
@Getter
@Setter
public class AlterarSenhaRequest {

    @NotBlank(message = "A senha atual é obrigatória.")
    private String senhaAtual;

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 8, max = 100, message = "A nova senha deve ter entre 8 e 100 caracteres.")
    private String novaSenha;
}

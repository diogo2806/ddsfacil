package br.com.ddsfacil.usuario.infrastructure.dto;

import br.com.ddsfacil.usuario.domain.PerfilUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para atualização de nome, perfil e status de um usuário")
@Getter
@Setter
public class UsuarioAtualizacaoRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
    private String nome;

    @NotNull(message = "O perfil é obrigatório.")
    private PerfilUsuario perfil;

    @NotNull(message = "O status (ativo) é obrigatório.")
    private Boolean ativo;
}

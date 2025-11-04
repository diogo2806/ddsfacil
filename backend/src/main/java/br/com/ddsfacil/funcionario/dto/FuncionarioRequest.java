// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/dto/FuncionarioRequest.java
package br.com.ddsfacil.funcionario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para criação de um novo funcionário")
@Getter
@Setter
public class FuncionarioRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    @Schema(description = "Nome completo do funcionário", example = "Ana Souza", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @NotBlank(message = "O celular é obrigatório.")
    @Size(max = 20, message = "O celular deve ter no máximo 20 caracteres.")
    @Pattern(regexp = "[0-9()+\\-\\s]+", message = "Informe um celular válido, utilizando apenas números e símbolos permitidos.")
    @Schema(description = "Celular com DDD para envio de SMS", example = "(21) 99999-8888", requiredMode = Schema.RequiredMode.REQUIRED)
    private String celular;

    @NotNull(message = "O local de trabalho é obrigatório.")
    @Schema(description = "ID do local de trabalho", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long localTrabalhoId;
}
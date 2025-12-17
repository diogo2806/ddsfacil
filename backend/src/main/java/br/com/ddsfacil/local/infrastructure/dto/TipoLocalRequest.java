// Arquivo: backend/src/main/java/br/com/ddsfacil/local/infrastructure/dto/TipoLocalRequest.java
package br.com.ddsfacil.local.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para criar ou atualizar um Tipo de Local")
@Getter
@Setter
public class TipoLocalRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    @Schema(description = "Nome do tipo (Ex: 'Obra', 'Escritório')", example = "Obra", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;
}
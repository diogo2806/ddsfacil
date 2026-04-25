// Arquivo: backend/src/main/java/br/com/ddsfacil/local/infrastructure/dto/LocalTrabalhoRequest.java
package br.com.ddsfacil.local.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para criação de um Local de Trabalho")
@Getter
@Setter
public class LocalTrabalhoRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 120)
    @Schema(description = "Nome do local", example = "Obra Centro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @NotNull(message = "O ID do tipo de local é obrigatório.")
    @Schema(description = "ID do Tipo de Local (ex: 'Obra', 'Escritório')", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long tipoLocalId;
}
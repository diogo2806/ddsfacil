// Arquivo: backend/src/main/java/br/com/ddsfacil/envio/dto/EnvioDdsRequest.java
package br.com.ddsfacil.envio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para registrar um novo envio de DDS")
@Getter
@Setter
public class EnvioDdsRequest {

    @NotNull(message = "O conteúdo é obrigatório.")
    @Schema(description = "ID do conteúdo a ser enviado", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long conteudoId;

    @NotEmpty(message = "Selecione pelo menos um funcionário.")
    @Size(max = 500, message = "Selecione no máximo 500 funcionários por envio.")
    @Schema(description = "Lista de IDs dos funcionários que receberão o DDS", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> funcionariosIds;

    @Schema(description = "Data do envio (opcional, padrão: data atual)", example = "2025-11-04")
    private LocalDate dataEnvio;
}
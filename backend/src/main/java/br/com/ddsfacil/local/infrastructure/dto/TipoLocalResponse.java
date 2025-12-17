// Arquivo: backend/src/main/java/br/com/ddsfacil/local/infrastructure/dto/TipoLocalResponse.java
package br.com.ddsfacil.local.infrastructure.dto;

import br.com.ddsfacil.local.domain.TipoLocal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "DTO de resposta para um Tipo de Local")
@Getter
public class TipoLocalResponse {

    @Schema(description = "ID único do tipo", example = "1")
    private final Long id;

    @Schema(description = "Nome do tipo", example = "Obra")
    private final String nome;

    public TipoLocalResponse(TipoLocal tipoLocal) {
        this.id = tipoLocal.getId();
        this.nome = tipoLocal.getNome();
    }
}
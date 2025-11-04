// Arquivo: backend/src/main/java/br/com/ddsfacil/local/dto/LocalTrabalhoResponse.java
package br.com.ddsfacil.local.dto;

import br.com.ddsfacil.local.LocalTrabalho;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "DTO de resposta para um Local de Trabalho")
@Getter
public class LocalTrabalhoResponse {

    @Schema(description = "ID único do local", example = "1")
    private final Long id;

    @Schema(description = "Nome do local", example = "Obra Centro")
    private final String nome;

    @Schema(description = "ID do tipo de local", example = "1")
    private final Long tipoLocalId;

    @Schema(description = "Nome do tipo de local", example = "Obra")
    private final String tipoLocalNome;

    public LocalTrabalhoResponse(LocalTrabalho local) {
        this.id = local.getId();
        this.nome = local.getNome();
        this.tipoLocalId = local.getTipoLocal().getId();
        this.tipoLocalNome = local.getTipoLocal().getNome();
    }
}
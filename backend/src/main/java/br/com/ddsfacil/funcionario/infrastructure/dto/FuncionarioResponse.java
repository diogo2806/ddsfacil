// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/infrastructure/dto/FuncionarioResponse.java
package br.com.ddsfacil.funcionario.infrastructure.dto;

import br.com.ddsfacil.funcionario.domain.FuncionarioEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "DTO de resposta para um funcionário cadastrado")
@Getter
public class FuncionarioResponse {

    @Schema(description = "ID único do funcionário", example = "1")
    private final Long id;

    @Schema(description = "Nome completo do funcionário", example = "Ana Souza")
    private final String nome;

    @Schema(description = "Celular com DDD", example = "(21) 99999-8888")
    private final String celular;

    @Schema(description = "ID do local de trabalho", example = "1")
    private final Long localTrabalhoId;

    @Schema(description = "Nome do local de trabalho", example = "Obra Centro")
    private final String localTrabalhoNome;

    @Schema(description = "Nome do tipo de local", example = "Obra")
    private final String tipoLocalNome;

    public FuncionarioResponse(FuncionarioEntity funcionario) {
        this.id = funcionario.getId();
        this.nome = funcionario.getNome();
        this.celular = funcionario.getCelular();
        this.localTrabalhoId = funcionario.getLocalTrabalho().getId();
        this.localTrabalhoNome = funcionario.getLocalTrabalho().getNome();
        this.tipoLocalNome = funcionario.getLocalTrabalho().getTipoLocal().getNome();
    }
}
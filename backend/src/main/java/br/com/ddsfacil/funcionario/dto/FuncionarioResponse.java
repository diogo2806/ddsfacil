// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/dto/FuncionarioResposta.java
package br.com.ddsfacil.funcionario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de resposta para um funcionário cadastrado")
public class FuncionarioResposta {

    @Schema(description = "ID único do funcionário", example = "1")
    private final Long id;

    @Schema(description = "Nome completo do funcionário", example = "Ana Souza")
    private final String nome;

    @Schema(description = "Celular com DDD", example = "(21) 99999-8888")
    private final String celular;

    @Schema(description = "Nome da obra ou centro de custo", example = "Obra Centro")
    private final String obra;

    public FuncionarioResposta(Long id, String nome, String celular, String obra) {
        this.id = id;
        this.nome = nome;
        this.celular = celular;
        this.obra = obra;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCelular() {
        return celular;
    }

    public String getObra() {
        return obra;
    }
}
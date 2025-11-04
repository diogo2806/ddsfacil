// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/dto/FuncionarioRequisicao.java
package br.com.ddsfacil.funcionario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para criação de um novo funcionário")
public class FuncionarioRequisicao {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    @Schema(description = "Nome completo do funcionário", example = "Ana Souza", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @NotBlank(message = "O celular é obrigatório.")
    @Size(max = 20, message = "O celular deve ter no máximo 20 caracteres.")
    @Pattern(regexp = "[0-9()+\\-\\s]+", message = "Informe um celular válido, utilizando apenas números e símbolos permitidos.")
    @Schema(description = "Celular com DDD para envio de SMS", example = "(21) 99999-8888", requiredMode = Schema.RequiredMode.REQUIRED)
    private String celular;

    @NotBlank(message = "A obra é obrigatória.")
    @Size(max = 120, message = "A obra deve ter no máximo 120 caracteres.")
    @Schema(description = "Nome da obra ou centro de custo", example = "Obra Centro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String obra;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getObra() {
        return obra;
    }

    public void setObra(String obra) {
        this.obra = obra;
    }
}
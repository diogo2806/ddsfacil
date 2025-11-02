package br.com.ddsfacil.envio.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public class EnvioDdsRequisicao {

    @NotNull(message = "O conteúdo é obrigatório.")
    private Long conteudoId;

    @NotEmpty(message = "Selecione pelo menos um funcionário.")
    @Size(max = 500, message = "Selecione no máximo 500 funcionários por envio.")
    private List<Long> funcionariosIds;

    private LocalDate dataEnvio;

    public Long getConteudoId() {
        return conteudoId;
    }

    public void setConteudoId(Long conteudoId) {
        this.conteudoId = conteudoId;
    }

    public List<Long> getFuncionariosIds() {
        return funcionariosIds;
    }

    public void setFuncionariosIds(List<Long> funcionariosIds) {
        this.funcionariosIds = funcionariosIds;
    }

    public LocalDate getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDate dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
}

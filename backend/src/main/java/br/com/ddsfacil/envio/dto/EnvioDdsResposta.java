package br.com.ddsfacil.envio.dto;

import br.com.ddsfacil.envio.StatusEnvioDds;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EnvioDdsResposta {

    private final Long id;
    private final Long funcionarioId;
    private final String nomeFuncionario;
    private final String obra;
    private final Long conteudoId;
    private final String tituloConteudo;
    private final StatusEnvioDds status;
    private final LocalDate dataEnvio;
    private final LocalDateTime momentoEnvio;
    private final LocalDateTime momentoConfirmacao;

    public EnvioDdsResposta(
        Long id,
        Long funcionarioId,
        String nomeFuncionario,
        String obra,
        Long conteudoId,
        String tituloConteudo,
        StatusEnvioDds status,
        LocalDate dataEnvio,
        LocalDateTime momentoEnvio,
        LocalDateTime momentoConfirmacao
    ) {
        this.id = id;
        this.funcionarioId = funcionarioId;
        this.nomeFuncionario = nomeFuncionario;
        this.obra = obra;
        this.conteudoId = conteudoId;
        this.tituloConteudo = tituloConteudo;
        this.status = status;
        this.dataEnvio = dataEnvio;
        this.momentoEnvio = momentoEnvio;
        this.momentoConfirmacao = momentoConfirmacao;
    }

    public Long getId() {
        return id;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public String getNomeFuncionario() {
        return nomeFuncionario;
    }

    public String getObra() {
        return obra;
    }

    public Long getConteudoId() {
        return conteudoId;
    }

    public String getTituloConteudo() {
        return tituloConteudo;
    }

    public StatusEnvioDds getStatus() {
        return status;
    }

    public LocalDate getDataEnvio() {
        return dataEnvio;
    }

    public LocalDateTime getMomentoEnvio() {
        return momentoEnvio;
    }

    public LocalDateTime getMomentoConfirmacao() {
        return momentoConfirmacao;
    }
}

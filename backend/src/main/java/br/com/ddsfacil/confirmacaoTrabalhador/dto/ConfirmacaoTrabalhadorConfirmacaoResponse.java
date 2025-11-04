package br.com.ddsfacil.confirmacaoTrabalhador.dto;

import br.com.ddsfacil.envio.StatusEnvioDds;
import java.time.LocalDateTime;

public class ConfirmacaoTrabalhadorConfirmacaoResponse {

    private final StatusEnvioDds status;
    private final LocalDateTime momentoConfirmacao;

    public ConfirmacaoTrabalhadorConfirmacaoResponse(StatusEnvioDds status, LocalDateTime momentoConfirmacao) {
        this.status = status;
        this.momentoConfirmacao = momentoConfirmacao;
    }

    public StatusEnvioDds getStatus() {
        return status;
    }

    public LocalDateTime getMomentoConfirmacao() {
        return momentoConfirmacao;
    }
}

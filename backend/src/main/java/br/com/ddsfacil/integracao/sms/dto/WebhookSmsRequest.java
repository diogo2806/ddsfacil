package br.com.ddsfacil.integracao.sms.dto;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.util.StringUtils;

public class WebhookSmsRequest {

    private Long envioId;
    private String messageStatus;
    private String smsStatus;
    private String errorCode;
    private String errorMessage;

    public Long getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Long envioId) {
        this.envioId = envioId;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getSmsStatus() {
        return smsStatus;
    }

    public void setSmsStatus(String smsStatus) {
        this.smsStatus = smsStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean indicaFalha() {
        String statusNormalizado = normalizarStatus();
        return "FAILED".equals(statusNormalizado)
                || "UNDELIVERED".equals(statusNormalizado)
                || "CANCELED".equals(statusNormalizado);
    }

    public String gerarDescricaoFalha() {
        String mensagem = String.format(
                "Falha de entrega (%s). Código: %s. Detalhe: %s",
                normalizarStatus(),
                valorOuPadrao(errorCode),
                valorOuPadrao(errorMessage)
        );
        return Jsoup.clean(mensagem, Safelist.none());
    }

    private String normalizarStatus() {
        String status = StringUtils.hasText(messageStatus) ? messageStatus : smsStatus;
        return status == null ? "DESCONHECIDO" : status.trim().toUpperCase();
    }

    private String valorOuPadrao(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : "N/I";
    }
}

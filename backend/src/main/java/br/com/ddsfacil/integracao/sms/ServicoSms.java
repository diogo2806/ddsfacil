package br.com.ddsfacil.integracao.sms;

public interface ServicoSms {
    void enviarMensagem(String numeroDestino, String mensagem, Long envioId);
}

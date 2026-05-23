package br.com.ddsfacil.integracao.mensageria;

import br.com.ddsfacil.envio.domain.CanalMensagem;
import br.com.ddsfacil.integracao.sms.IntegracaoSmsPropriedades;
import br.com.ddsfacil.integracao.whatsapp.IntegracaoWhatsappPropriedades;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class TwilioServicoMensageria implements ServicoMensageria {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioServicoMensageria.class);
    private static final String PREFIXO_WHATSAPP = "whatsapp:";

    private final IntegracaoSmsPropriedades smsPropriedades;
    private final IntegracaoWhatsappPropriedades whatsappPropriedades;
    private final RestClient clienteSms;
    private final RestClient clienteWhatsapp;

    public TwilioServicoMensageria(
            RestClient.Builder builder,
            IntegracaoSmsPropriedades smsPropriedades,
            IntegracaoWhatsappPropriedades whatsappPropriedades
    ) {
        this.smsPropriedades = smsPropriedades;
        this.whatsappPropriedades = whatsappPropriedades;
        this.clienteSms = smsPropriedades.isHabilitado() && smsPropriedades.credenciaisCompletas()
                ? construirCliente(builder, smsPropriedades.getContaSid(), smsPropriedades.getTokenAutenticacao())
                : null;
        this.clienteWhatsapp = whatsappPropriedades.isHabilitado() && whatsappPropriedades.credenciaisCompletas()
                ? construirCliente(builder.clone(), whatsappPropriedades.getContaSid(), whatsappPropriedades.getTokenAutenticacao())
                : null;
    }

    @Override
    public void enviarMensagem(CanalMensagem canal, String numeroDestino, String mensagem, Long envioId) {
        CanalMensagem canalEfetivo = canal == null ? CanalMensagem.SMS : canal;
        if (canalEfetivo == CanalMensagem.WHATSAPP) {
            enviarViaWhatsapp(numeroDestino, mensagem, envioId);
        } else {
            enviarViaSms(numeroDestino, mensagem, envioId);
        }
    }

    private void enviarViaSms(String numeroDestino, String mensagem, Long envioId) {
        if (!smsPropriedades.isHabilitado()) {
            LOGGER.info("Envio de SMS desabilitado. Mensagem para {} não será enviada.", numeroDestino);
            return;
        }
        if (clienteSms == null) {
            LOGGER.warn("Credenciais de SMS incompletas. Mensagem para {} não enviada.", numeroDestino);
            return;
        }
        String statusCallback = smsPropriedades.montarUrlStatusCallback(envioId);
        postarMensagem(clienteSms, numeroDestino, smsPropriedades.getNumeroOrigem(), mensagem, statusCallback, "SMS");
    }

    private void enviarViaWhatsapp(String numeroDestino, String mensagem, Long envioId) {
        if (!whatsappPropriedades.isHabilitado()) {
            LOGGER.info("Envio de WhatsApp desabilitado. Mensagem para {} não será enviada.", numeroDestino);
            return;
        }
        if (clienteWhatsapp == null) {
            LOGGER.warn("Credenciais de WhatsApp incompletas. Mensagem para {} não enviada.", numeroDestino);
            return;
        }
        String origem = PREFIXO_WHATSAPP + whatsappPropriedades.getNumeroOrigem();
        String destino = PREFIXO_WHATSAPP + numeroDestino;
        // O webhook de status é compartilhado com o SMS (mesmo provedor Twilio).
        String statusCallback = smsPropriedades.montarUrlStatusCallback(envioId);
        postarMensagem(clienteWhatsapp, destino, origem, mensagem, statusCallback, "WhatsApp");
    }

    private void postarMensagem(
            RestClient cliente,
            String para,
            String de,
            String mensagem,
            String statusCallback,
            String rotuloCanal
    ) {
        MultiValueMap<String, String> corpo = new LinkedMultiValueMap<>();
        corpo.add("To", para);
        corpo.add("From", de);
        corpo.add("Body", mensagem);
        if (statusCallback != null) {
            corpo.add("StatusCallback", statusCallback);
        }
        try {
            cliente
                    .post()
                    .uri("/Messages.json")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(corpo)
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.info("{} enviado para {} com sucesso.", rotuloCanal, para);
        } catch (Exception ex) {
            LOGGER.error("Falha ao enviar {} para {}", rotuloCanal, para, ex);
        }
    }

    private RestClient construirCliente(RestClient.Builder builder, String contaSid, String token) {
        String urlBase = "https://api.twilio.com/2010-04-01/Accounts/" + contaSid;
        String autorizacao = HttpHeaders.encodeBasicAuth(contaSid, token, StandardCharsets.UTF_8);
        return builder
                .baseUrl(urlBase)
                .defaultHeader(HttpHeaders.AUTHORIZATION, autorizacao)
                .build();
    }
}

package br.com.ddsfacil.integracao.sms;

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
public class TwilioServicoSms implements ServicoSms {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioServicoSms.class);

    private final IntegracaoSmsPropriedades propriedades;
    private final RestClient cliente;

    public TwilioServicoSms(RestClient.Builder builder, IntegracaoSmsPropriedades propriedades) {
        this.propriedades = propriedades;
        if (propriedades.isHabilitado() && propriedades.credenciaisCompletas()) {
            String urlBase = "https://api.twilio.com/2010-04-01/Accounts/" + propriedades.getContaSid();
            String cabecalhoAutorizacao = HttpHeaders.encodeBasicAuth(
                propriedades.getContaSid(),
                propriedades.getTokenAutenticacao(),
                StandardCharsets.UTF_8
            );
            this.cliente = builder
                .baseUrl(urlBase)
                .defaultHeader(HttpHeaders.AUTHORIZATION, cabecalhoAutorizacao)
                .build();
        } else {
            this.cliente = null;
        }
    }

    @Override
    public void enviarMensagem(String numeroDestino, String mensagem, Long envioId) {
        if (!propriedades.isHabilitado()) {
            LOGGER.info("Envio de SMS desabilitado. Mensagem para {} não será enviada.", numeroDestino);
            return;
        }
        if (!propriedades.credenciaisCompletas()) {
            LOGGER.warn("Credenciais do provedor de SMS incompletas. Mensagem para {} não enviada.", numeroDestino);
            return;
        }
        if (cliente == null) {
            LOGGER.error("Cliente HTTP do provedor de SMS não foi inicializado.");
            return;
        }
        MultiValueMap<String, String> corpo = new LinkedMultiValueMap<>();
        corpo.add("To", numeroDestino);
        corpo.add("From", propriedades.getNumeroOrigem());
        corpo.add("Body", mensagem);
        String statusCallback = propriedades.montarUrlStatusCallback(envioId);
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
            LOGGER.info("SMS enviado para {} com sucesso.", numeroDestino);
        } catch (Exception ex) {
            LOGGER.error("Falha ao enviar SMS para {}", numeroDestino, ex);
        }
    }
}

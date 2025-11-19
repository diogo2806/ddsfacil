package br.com.ddsfacil.integracao.sms;

import br.com.ddsfacil.envio.EnvioDdsRepository;
import br.com.ddsfacil.integracao.sms.dto.WebhookSmsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/webhooks/sms")
public class WebhookSmsPublicoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookSmsPublicoController.class);
    private final EnvioDdsRepository envioDdsRepository;

    public WebhookSmsPublicoController(EnvioDdsRepository envioDdsRepository) {
        this.envioDdsRepository = envioDdsRepository;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> receberAtualizacaoFormulario(WebhookSmsRequest payload) {
        return processar(payload);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> receberAtualizacaoJson(@RequestBody WebhookSmsRequest payload) {
        return processar(payload);
    }

    private ResponseEntity<Void> processar(WebhookSmsRequest payload) {
        if (payload.getEnvioId() == null) {
            LOGGER.warn("Webhook recebido sem o parâmetro envioId.");
            return ResponseEntity.badRequest().build();
        }
        envioDdsRepository.findById(payload.getEnvioId()).ifPresentOrElse(envio -> {
            if (payload.indicaFalha()) {
                envio.registrarFalhaEntrega(payload.gerarDescricaoFalha());
                envioDdsRepository.save(envio);
                LOGGER.info("Envio {} marcado como falha através do webhook.", envio.getId());
            }
        }, () -> LOGGER.warn("Envio {} não encontrado para atualização de webhook.", payload.getEnvioId()));
        return ResponseEntity.ok().build();
    }
}

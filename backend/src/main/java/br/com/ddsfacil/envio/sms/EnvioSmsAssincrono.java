package br.com.ddsfacil.envio.sms;

import br.com.ddsfacil.envio.EnvioDds;
import br.com.ddsfacil.integracao.sms.IntegracaoSmsPropriedades;
import br.com.ddsfacil.integracao.sms.ServicoSms;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EnvioSmsAssincrono {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvioSmsAssincrono.class);

    private final ServicoSms servicoSms;
    private final IntegracaoSmsPropriedades propriedades;

    public EnvioSmsAssincrono(ServicoSms servicoSms, IntegracaoSmsPropriedades propriedades) {
        this.servicoSms = servicoSms;
        this.propriedades = propriedades;
    }

    @Async
    public void enviarMensagens(List<EnvioDds> envios) {
        if (envios == null || envios.isEmpty()) {
            return;
        }
        if (!propriedades.urlConfirmacaoValida()) {
            LOGGER.warn("URL base de confirmação não configurada. Mensagens de SMS não serão enviadas.");
            return;
        }
        String urlBase = normalizarUrlBase(propriedades.getUrlBaseConfirmacao());
        for (EnvioDds envio : envios) {
            try {
                String numeroDestino = sanitizarNumero(envio.getFuncionario().getCelular());
                if (!StringUtils.hasText(numeroDestino)) {
                    LOGGER.warn("Número de telefone inválido para o funcionário {}.", envio.getFuncionario().getId());
                    continue;
                }
                String titulo = Jsoup.clean(envio.getConteudo().getTitulo(), Safelist.none()).strip();
                String linkConfirmacao = urlBase + envio.getTokenAcesso();
                String mensagem = "DDS: " + titulo + ". Leia e confirme: " + linkConfirmacao;
                servicoSms.enviarMensagem(numeroDestino, mensagem);
            } catch (Exception ex) {
                LOGGER.error("Erro ao preparar envio de SMS para o envio {}.", envio.getId(), ex);
            }
        }
    }

    private String normalizarUrlBase(String urlBase) {
        String urlLimpa = urlBase.trim();
        if (!urlLimpa.endsWith("/")) {
            urlLimpa = urlLimpa + "/";
        }
        return urlLimpa;
    }

    private String sanitizarNumero(String numero) {
        if (numero == null) {
            return null;
        }
        String numeroLimpo = numero.replaceAll("[^0-9+]", "");
        if (!numeroLimpo.startsWith("+") && numeroLimpo.length() == 11) {
            return "+55" + numeroLimpo;
        }
        return numeroLimpo;
    }
}

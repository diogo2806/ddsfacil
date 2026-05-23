package br.com.ddsfacil.envio.infrastructure.sms;

import br.com.ddsfacil.envio.domain.EnvioDdsEntity;
import br.com.ddsfacil.envio.infrastructure.EnvioDdsRepository;
import br.com.ddsfacil.envio.domain.StatusEnvioDds;
import br.com.ddsfacil.envio.infrastructure.lembrete.LembretePropriedades;
import br.com.ddsfacil.integracao.sms.IntegracaoSmsPropriedades;
import br.com.ddsfacil.integracao.sms.ServicoSms;
import br.com.ddsfacil.licenca.application.LicencaService;
import java.time.LocalDateTime;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EnvioSmsProcessadorDeJobs {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvioSmsProcessadorDeJobs.class);

    private final EnvioDdsRepository envioRepositorio;
    private final ServicoSms servicoSms;
    private final IntegracaoSmsPropriedades propriedades;
    private final LicencaService licencaService;
    private final LembretePropriedades lembretePropriedades;

    public EnvioSmsProcessadorDeJobs(
            EnvioDdsRepository envioRepositorio,
            ServicoSms servicoSms,
            IntegracaoSmsPropriedades propriedades,
            LicencaService licencaService,
            LembretePropriedades lembretePropriedades
    ) {
        this.envioRepositorio = envioRepositorio;
        this.servicoSms = servicoSms;
        this.propriedades = propriedades;
        this.licencaService = licencaService;
        this.lembretePropriedades = lembretePropriedades;
    }

    @Job(name = "Envio SMS %0", retries = 2)
    public void processarEnvioUnitario(Long envioId) {
        if (envioId == null) {
            return;
        }
        if (!propriedades.urlConfirmacaoValida()) {
            LOGGER.warn("URL base de confirmação não configurada. Mensagens de SMS não serão enviadas.");
            return;
        }

        envioRepositorio.findById(envioId).ifPresentOrElse(envio -> {
            if (envio.getStatus() == StatusEnvioDds.CONFIRMADO) {
                LOGGER.info("Envio {} já confirmado. Job ignorado.", envioId);
                return;
            }
            if (envio.getStatus() == StatusEnvioDds.ENVIADO) {
                LOGGER.info("Envio {} já marcado como enviado. Job ignorado.", envioId);
                return;
            }
            String urlBase = normalizarUrlBase(propriedades.getUrlBaseConfirmacao());
            enviarSms(envio, urlBase);
        }, () -> LOGGER.warn("Envio com ID {} não encontrado para processamento do SMS.", envioId));
    }

    @Job(name = "Lembrete DDS %0", retries = 1)
    @Transactional
    public void processarLembreteUnitario(Long envioId) {
        if (envioId == null) {
            return;
        }
        if (!propriedades.urlConfirmacaoValida()) {
            LOGGER.warn("URL base de confirmação não configurada. Lembretes de SMS não serão enviados.");
            return;
        }
        envioRepositorio.findById(envioId).ifPresentOrElse(envio -> {
            if (envio.getStatus() != StatusEnvioDds.ENVIADO) {
                return;
            }
            if (envio.getQuantidadeLembretes() >= lembretePropriedades.getMaximoLembretes()) {
                return;
            }
            if (!licencaService.possuiCreditoParaEnvio(envio.getEmpresaId())) {
                LOGGER.info("Sem saldo/licença válida para lembrete do envio {}.", envioId);
                return;
            }
            licencaService.debitarSms(envio.getEmpresaId(), 1);
            String urlBase = normalizarUrlBase(propriedades.getUrlBaseConfirmacao());
            enviarLembrete(envio, urlBase);
            envio.registrarLembrete(LocalDateTime.now());
            envioRepositorio.save(envio);
        }, () -> LOGGER.warn("Envio com ID {} não encontrado para processamento do lembrete.", envioId));
    }

    private void enviarLembrete(EnvioDdsEntity envio, String urlBase) {
        String numeroDestino = sanitizarNumero(envio.getFuncionarioEntity().getCelular());
        if (!StringUtils.hasText(numeroDestino)) {
            LOGGER.warn("Número de telefone inválido para lembrete do funcionário {}.", envio.getFuncionarioEntity().getId());
            return;
        }
        String titulo = Jsoup.clean(envio.getConteudo().getTitulo(), Safelist.none()).strip();
        String linkConfirmacao = urlBase + envio.getTokenAcesso();
        String mensagem = "Lembrete DDS: " + titulo + ". Você ainda não confirmou. Leia e confirme: " + linkConfirmacao;
        servicoSms.enviarMensagem(numeroDestino, mensagem, envio.getId());
        LOGGER.info("Lembrete de SMS enviado para o envio {} (lembrete #{}).", envio.getId(), envio.getQuantidadeLembretes() + 1);
    }

    private void enviarSms(EnvioDdsEntity envio, String urlBase) {
        try {
            String numeroDestino = sanitizarNumero(envio.getFuncionarioEntity().getCelular());
            if (!StringUtils.hasText(numeroDestino)) {
                LOGGER.warn("Número de telefone inválido para o funcionário {}.", envio.getFuncionarioEntity().getId());
                envio.registrarFalhaEntrega("Número de celular inválido para envio de SMS.");
                envioRepositorio.save(envio);
                return;
            }
            String titulo = Jsoup.clean(envio.getConteudo().getTitulo(), Safelist.none()).strip();
            String linkConfirmacao = urlBase + envio.getTokenAcesso();
            String mensagem = "DDS: " + titulo + ". Leia e confirme: " + linkConfirmacao;
            servicoSms.enviarMensagem(numeroDestino, mensagem, envio.getId());
            envio.marcarComoEnviado(LocalDateTime.now());
            envioRepositorio.save(envio);
            LOGGER.info("SMS enviado para o envio {}.", envio.getId());
        } catch (Exception ex) {
            envio.registrarFalhaEntrega("Erro no envio do SMS: " + ex.getMessage());
            envioRepositorio.save(envio);
            LOGGER.error("Erro ao enviar SMS para o envio {}.", envio.getId(), ex);
        }
    }

    private String normalizarUrlBase(String urlBase) {
        String urlLimpa = urlBase.trim();
        boolean terminaComSegmentoConfirmacao = urlLimpa.matches(".*/c/?$");
        if (!terminaComSegmentoConfirmacao) {
            urlLimpa = urlLimpa.replaceAll("/+\\z", "");
            urlLimpa = urlLimpa + "/c/";
            return urlLimpa;
        }

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

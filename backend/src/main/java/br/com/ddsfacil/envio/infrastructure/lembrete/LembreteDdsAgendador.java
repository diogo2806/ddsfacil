package br.com.ddsfacil.envio.infrastructure.lembrete;

import br.com.ddsfacil.envio.domain.StatusEnvioDds;
import br.com.ddsfacil.envio.infrastructure.EnvioDdsRepository;
import br.com.ddsfacil.envio.infrastructure.sms.EnvioSmsProcessadorDeJobs;
import br.com.ddsfacil.integracao.sms.IntegracaoSmsPropriedades;
import java.time.LocalDateTime;
import java.util.List;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LembreteDdsAgendador {

    private static final Logger LOGGER = LoggerFactory.getLogger(LembreteDdsAgendador.class);

    private final EnvioDdsRepository envioRepositorio;
    private final EnvioSmsProcessadorDeJobs processadorDeJobs;
    private final JobScheduler agendadorDeJobs;
    private final LembretePropriedades lembretePropriedades;
    private final IntegracaoSmsPropriedades smsPropriedades;

    public LembreteDdsAgendador(
            EnvioDdsRepository envioRepositorio,
            EnvioSmsProcessadorDeJobs processadorDeJobs,
            JobScheduler agendadorDeJobs,
            LembretePropriedades lembretePropriedades,
            IntegracaoSmsPropriedades smsPropriedades
    ) {
        this.envioRepositorio = envioRepositorio;
        this.processadorDeJobs = processadorDeJobs;
        this.agendadorDeJobs = agendadorDeJobs;
        this.lembretePropriedades = lembretePropriedades;
        this.smsPropriedades = smsPropriedades;
    }

    @Recurring(id = "lembretes-dds-pendentes", interval = "PT1H")
    @Job(name = "Agendar lembretes de DDS pendentes")
    public void agendarLembretes() {
        if (!lembretePropriedades.isHabilitado()) {
            return;
        }
        if (!smsPropriedades.urlConfirmacaoValida()) {
            LOGGER.warn("URL base de confirmação não configurada. Lembretes automáticos não serão agendados.");
            return;
        }

        LocalDateTime limite = LocalDateTime.now().minusHours(lembretePropriedades.getIntervaloHoras());
        List<Long> idsParaLembrar = envioRepositorio.buscarIdsParaLembrete(
                StatusEnvioDds.ENVIADO,
                lembretePropriedades.getMaximoLembretes(),
                limite
        );

        if (idsParaLembrar.isEmpty()) {
            return;
        }
        LOGGER.info("Agendando {} lembrete(s) de DDS pendente(s).", idsParaLembrar.size());
        idsParaLembrar.forEach(id ->
                agendadorDeJobs.enqueue(() -> processadorDeJobs.processarLembreteUnitario(id))
        );
    }
}

// Arquivo: backend/src/main/java/br/com/ddsfacil/envio/EnvioDdsService.java
package br.com.ddsfacil.envio;

import br.com.ddsfacil.configuracao.multitenant.ContextoEmpresa;
import br.com.ddsfacil.conteudo.ConteudoDdsEntity;
import br.com.ddsfacil.conteudo.ConteudoDdsRepository;
import br.com.ddsfacil.envio.dto.EnvioDdsRequest;
import br.com.ddsfacil.envio.dto.EnvioDdsResponse;
import br.com.ddsfacil.envio.sms.EnvioSmsProcessadorDeJobs;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.funcionario.FuncionarioEntity;
import br.com.ddsfacil.funcionario.FuncionarioRepository;
import br.com.ddsfacil.licenca.LicencaService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvioDdsService {

    private static final Logger log = LoggerFactory.getLogger(EnvioDdsService.class);
    private final EnvioDdsRepository envioRepositorio;
    private final FuncionarioRepository funcionarioRepository;
    private final ConteudoDdsRepository conteudoRepositorio;
    private final JobScheduler agendadorDeJobs;
    private final EnvioSmsProcessadorDeJobs processadorDeJobs;
    private final LicencaService licencaService;

    public EnvioDdsService(
            EnvioDdsRepository envioRepositorio,
            FuncionarioRepository funcionarioRepository,
            ConteudoDdsRepository conteudoRepositorio,
            JobScheduler agendadorDeJobs,
            EnvioSmsProcessadorDeJobs processadorDeJobs,
            LicencaService licencaService
    ) {
        this.envioRepositorio = envioRepositorio;
        this.funcionarioRepository = funcionarioRepository;
        this.conteudoRepositorio = conteudoRepositorio;
        this.agendadorDeJobs = agendadorDeJobs;
        this.processadorDeJobs = processadorDeJobs;
        this.licencaService = licencaService;
    }

    @Transactional
    public List<EnvioDdsResponse> enviar(EnvioDdsRequest requisicao) {
        Objects.requireNonNull(requisicao, "Requisição não pode ser nula.");
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();

        ConteudoDdsEntity conteudo = conteudoRepositorio
                .findById(requisicao.getConteudoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo não encontrado."));

        List<Long> idsFuncionarios = requisicao.getFuncionariosIds();
        log.info("Iniciando envio de DDS para {} funcionário(s) sobre o conteúdo ID: {}", idsFuncionarios.size(), requisicao.getConteudoId());

        List<FuncionarioEntity> funcionarioEntities = funcionarioRepository.findAllById(idsFuncionarios);
        if (funcionarioEntities.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum funcionário encontrado para o envio informado.");
        }
        long quantidadeEsperada = idsFuncionarios.stream().distinct().count();
        if (funcionarioEntities.size() != quantidadeEsperada) {
            throw new RecursoNaoEncontradoException("Alguns funcionários informados não foram encontrados.");
        }

        LocalDate dataEnvio = requisicao.getDataEnvio() != null ? requisicao.getDataEnvio() : LocalDate.now();
        Map<Long, FuncionarioEntity> funcionariosPorId = funcionarioEntities
                .stream()
                .collect(Collectors.toMap(FuncionarioEntity::getId, funcionarioEntity -> funcionarioEntity, (primeiro, segundo) -> primeiro));

        List<EnvioDdsEntity> novosEnvios = new ArrayList<>();
        for (Long funcionarioId : idsFuncionarios) {
            FuncionarioEntity funcionarioEntity = funcionariosPorId.get(funcionarioId);
            if (funcionarioEntity == null) {
                continue;
            }

            // CORREÇÃO: Atualizando a chamada para o método renomeado
            boolean jaExiste = envioRepositorio
                    .findByDataEnvioAndFuncionarioEntityIdAndConteudoId(dataEnvio, funcionarioId, conteudo.getId())
                    .isPresent();

            if (jaExiste) {
                log.warn("Envio duplicado ignorado para funcionário ID: {} e conteúdo ID: {}", funcionarioId, conteudo.getId());
                continue;
            }
            LocalDateTime momentoEnvio = LocalDateTime.now();
            EnvioDdsEntity envio = new EnvioDdsEntity(funcionarioEntity, conteudo, dataEnvio, momentoEnvio, empresaId);
            novosEnvios.add(envio);
        }

        if (novosEnvios.isEmpty()) {
            log.warn("Nenhum novo envio a ser processado (funcionários já receberam ou não encontrados).");
            return List.of();
        }

        licencaService.debitarSms(empresaId, novosEnvios.size());
        List<EnvioDdsEntity> salvos = envioRepositorio.saveAll(novosEnvios);
        agendarEnvioDeSms(salvos);
        log.info("Envio de {} SMSs agendado no JobRunr.", salvos.size());

        return salvos.stream().map(this::mapearParaResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<EnvioDdsResponse> listarPorData(LocalDate data) {
        LocalDate dataConsulta = data != null ?
                data : LocalDate.now();
        return envioRepositorio
                .findByDataEnvioOrderByMomentoEnvioAsc(dataConsulta)
                .stream()
                .map(this::mapearParaResposta)
                .toList();
    }

    @Transactional
    public EnvioDdsResponse confirmar(Long id) {
        log.info("Confirmando envio ID: {}", id);
        EnvioDdsEntity envio = envioRepositorio
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Envio não encontrado."));

        if (envio.getStatus() == StatusEnvioDds.CONFIRMADO) {
            log.warn("Envio ID: {} já estava confirmado.", id);
            return mapearParaResposta(envio);
        }
        envio.confirmar(LocalDateTime.now());
        log.info("Envio ID: {} confirmado com sucesso.", id);
        return mapearParaResposta(envio);
    }

    private void agendarEnvioDeSms(List<EnvioDdsEntity> envios) {
        envios.forEach(envio -> agendadorDeJobs.enqueue(
                () -> processadorDeJobs.processarEnvioUnitario(envio.getId())
        ));
    }

    private EnvioDdsResponse mapearParaResposta(EnvioDdsEntity envio) {
        return new EnvioDdsResponse(
                envio.getId(),
                envio.getFuncionarioEntity().getId(),
                envio.getFuncionarioEntity().getNome(),
                envio.getFuncionarioEntity().getLocalTrabalho().getNome(),
                envio.getConteudo().getId(),
                envio.getConteudo().getTitulo(),
                envio.getStatus(),
                envio.getDataEnvio(),
                envio.getMomentoEnvio(),
                envio.getMomentoConfirmacao(),
                envio.getErroEntrega()
        );
    }
}
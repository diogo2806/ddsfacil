// Arquivo: backend/src/main/java/br/com/ddsfacil/envio/EnvioDdsServico.java
package br.com.ddsfacil.envio;

import br.com.ddsfacil.conteudo.ConteudoDds;
import br.com.ddsfacil.conteudo.ConteudoDdsRepositorio;
import br.com.ddsfacil.envio.dto.EnvioDdsRequisicao;
import br.com.ddsfacil.envio.dto.EnvioDdsResposta;
import br.com.ddsfacil.envio.sms.EnvioSmsAssincrono;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.funcionario.Funcionario;
import br.com.ddsfacil.funcionario.FuncionarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvioDdsServico {

    private static final Logger log = LoggerFactory.getLogger(EnvioDdsServico.class);
    private final EnvioDdsRepositorio envioRepositorio;
    private final FuncionarioRepository funcionarioRepository;
    private final ConteudoDdsRepositorio conteudoRepositorio;
    private final EnvioSmsAssincrono envioSmsAssincrono;

    public EnvioDdsServico(
            EnvioDdsRepositorio envioRepositorio,
            FuncionarioRepository funcionarioRepository,
            ConteudoDdsRepositorio conteudoRepositorio,
            EnvioSmsAssincrono envioSmsAssincrono
    ) {
        this.envioRepositorio = envioRepositorio;
        this.funcionarioRepository = funcionarioRepository;
        this.conteudoRepositorio = conteudoRepositorio;
        this.envioSmsAssincrono = envioSmsAssincrono;
    }

    @Transactional
    public List<EnvioDdsResposta> enviar(EnvioDdsRequisicao requisicao) {
        Objects.requireNonNull(requisicao, "Requisição não pode ser nula.");

        ConteudoDds conteudo = conteudoRepositorio
                .findById(requisicao.getConteudoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo não encontrado."));

        List<Long> idsFuncionarios = requisicao.getFuncionariosIds();
        log.info("Iniciando envio de DDS para {} funcionário(s) sobre o conteúdo ID: {}", idsFuncionarios.size(), requisicao.getConteudoId());

        List<Funcionario> funcionarios = funcionarioRepository.findAllById(idsFuncionarios);
        if (funcionarios.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum funcionário encontrado para o envio informado.");
        }
        long quantidadeEsperada = idsFuncionarios.stream().distinct().count();
        if (funcionarios.size() != quantidadeEsperada) {
            throw new RecursoNaoEncontradoException("Alguns funcionários informados não foram encontrados.");
        }

        LocalDate dataEnvio = requisicao.getDataEnvio() != null ? requisicao.getDataEnvio() : LocalDate.now();
        Map<Long, Funcionario> funcionariosPorId = funcionarios
                .stream()
                .collect(Collectors.toMap(Funcionario::getId, funcionario -> funcionario, (primeiro, segundo) -> primeiro));

        List<EnvioDds> novosEnvios = new ArrayList<>();
        for (Long funcionarioId : idsFuncionarios) {
            Funcionario funcionario = funcionariosPorId.get(funcionarioId);
            if (funcionario == null) {
                continue;
            }
            boolean jaExiste = envioRepositorio
                    .findByDataEnvioAndFuncionarioIdAndConteudoId(dataEnvio, funcionarioId, conteudo.getId())
                    .isPresent();
            if (jaExiste) {
                log.warn("Envio duplicado ignorado para funcionário ID: {} e conteúdo ID: {}", funcionarioId, conteudo.getId());
                continue;
            }
            LocalDateTime momentoEnvio = LocalDateTime.now();
            EnvioDds envio = new EnvioDds(funcionario, conteudo, dataEnvio, momentoEnvio);
            novosEnvios.add(envio);
        }

        if (novosEnvios.isEmpty()) {
            log.warn("Nenhum novo envio a ser processado (funcionários já receberam ou não encontrados).");
            return List.of();
        }

        List<EnvioDds> salvos = envioRepositorio.saveAll(novosEnvios);
        envioSmsAssincrono.enviarMensagens(salvos);
        log.info("Envio de {} SMSs iniciado de forma assíncrona.", salvos.size());

        return salvos.stream().map(this::mapearParaResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<EnvioDdsResposta> listarPorData(LocalDate data) {
        LocalDate dataConsulta = data != null ?
                data : LocalDate.now();
        return envioRepositorio
                .findByDataEnvioOrderByMomentoEnvioAsc(dataConsulta)
                .stream()
                .map(this::mapearParaResposta)
                .toList();
    }

    @Transactional
    public EnvioDdsResposta confirmar(Long id) {
        log.info("Confirmando envio ID: {}", id);
        EnvioDds envio = envioRepositorio
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

    private EnvioDdsResposta mapearParaResposta(EnvioDds envio) {
        return new EnvioDdsResposta(
                envio.getId(),
                envio.getFuncionario().getId(),
                envio.getFuncionario().getNome(),
                envio.getFuncionario().getObra(),
                envio.getConteudo().getId(),
                envio.getConteudo().getTitulo(),
                envio.getStatus(),
                envio.getDataEnvio(),
                envio.getMomentoEnvio(),
                envio.getMomentoConfirmacao()
        );
    }
}
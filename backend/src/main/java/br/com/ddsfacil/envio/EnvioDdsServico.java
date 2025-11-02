package br.com.ddsfacil.envio;

import br.com.ddsfacil.conteudo.ConteudoDds;
import br.com.ddsfacil.conteudo.ConteudoDdsRepositorio;
import br.com.ddsfacil.envio.dto.EnvioDdsRequisicao;
import br.com.ddsfacil.envio.dto.EnvioDdsResposta;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.funcionario.Funcionario;
import br.com.ddsfacil.funcionario.FuncionarioRepositorio;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvioDdsServico {

    private final EnvioDdsRepositorio envioRepositorio;
    private final FuncionarioRepositorio funcionarioRepositorio;
    private final ConteudoDdsRepositorio conteudoRepositorio;

    public EnvioDdsServico(
        EnvioDdsRepositorio envioRepositorio,
        FuncionarioRepositorio funcionarioRepositorio,
        ConteudoDdsRepositorio conteudoRepositorio
    ) {
        this.envioRepositorio = envioRepositorio;
        this.funcionarioRepositorio = funcionarioRepositorio;
        this.conteudoRepositorio = conteudoRepositorio;
    }

    @Transactional
    public List<EnvioDdsResposta> enviar(EnvioDdsRequisicao requisicao) {
        ConteudoDds conteudo = conteudoRepositorio
            .findById(requisicao.getConteudoId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo não encontrado."));

        List<Long> idsFuncionarios = requisicao.getFuncionariosIds();
        List<Funcionario> funcionarios = funcionarioRepositorio.findAllById(idsFuncionarios);
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
                continue;
            }
            LocalDateTime momentoEnvio = LocalDateTime.now();
            EnvioDds envio = new EnvioDds(funcionario, conteudo, dataEnvio, momentoEnvio);
            novosEnvios.add(envio);
        }

        List<EnvioDds> salvos = envioRepositorio.saveAll(novosEnvios);
        return salvos.stream().map(this::mapearParaResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<EnvioDdsResposta> listarPorData(LocalDate data) {
        LocalDate dataConsulta = data != null ? data : LocalDate.now();
        return envioRepositorio
            .findByDataEnvioOrderByMomentoEnvioAsc(dataConsulta)
            .stream()
            .map(this::mapearParaResposta)
            .toList();
    }

    @Transactional
    public EnvioDdsResposta confirmar(Long id) {
        EnvioDds envio = envioRepositorio
            .findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Envio não encontrado."));
        if (envio.getStatus() == StatusEnvioDds.CONFIRMADO) {
            return mapearParaResposta(envio);
        }
        envio.confirmar(LocalDateTime.now());
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

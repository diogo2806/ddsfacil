// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/application/FuncionarioService.java
package br.com.ddsfacil.funcionario.application;

import br.com.ddsfacil.configuracao.multitenant.ContextoEmpresa;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.funcionario.domain.FuncionarioEntity;
import br.com.ddsfacil.funcionario.infrastructure.FuncionarioRepository;
import br.com.ddsfacil.funcionario.infrastructure.dto.FuncionarioRequest;
import br.com.ddsfacil.funcionario.infrastructure.dto.FuncionarioResponse;
import br.com.ddsfacil.local.domain.LocalTrabalho;
import br.com.ddsfacil.local.infrastructure.LocalTrabalhoRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FuncionarioService {

    private static final Logger log = LoggerFactory.getLogger(FuncionarioService.class);
    private final FuncionarioRepository funcionarioRepository;
    private final LocalTrabalhoRepository localTrabalhoRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository, LocalTrabalhoRepository localTrabalhoRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.localTrabalhoRepository = localTrabalhoRepository;
    }

    @Transactional
    public FuncionarioResponse criar(FuncionarioRequest requisicao) {
        Objects.requireNonNull(requisicao, "Requisição não pode ser nula.");
        LocalTrabalho local = localTrabalhoRepository.findById(requisicao.getLocalTrabalhoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Local de trabalho não encontrado."));
        String nomeLimpo = sanitizarTexto(requisicao.getNome());
        String celularLimpo = sanitizarCelular(requisicao.getCelular());
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();

        log.info("Criando novo funcionário. Nome: {}, Local: {}", nomeLimpo, local.getNome());
        FuncionarioEntity funcionarioEntity = new FuncionarioEntity(nomeLimpo, celularLimpo, local, empresaId);
        FuncionarioEntity salvo = funcionarioRepository.save(funcionarioEntity);

        log.info("Funcionário criado com ID: {}. (LGPD: Dados Pessoais envolvidos)", salvo.getId());
        return mapearParaResposta(salvo);
    }

    @Transactional(readOnly = true)
    public List<FuncionarioResponse> listar(Long localTrabalhoId) {
        List<FuncionarioEntity> funcionarioEntities;
        if (localTrabalhoId != null) {
            // [REATORADO] Chamando o método derivado correto
            funcionarioEntities = funcionarioRepository.findAllByLocalTrabalhoIdOrderByNomeAsc(localTrabalhoId);
        } else {
            funcionarioEntities = funcionarioRepository.findAllByOrderByNomeAsc();
        }
        return funcionarioEntities.stream().map(this::mapearParaResposta).collect(Collectors.toList());
    }

    @Transactional
    public void remover(Long id) {
        log.info("Tentando remover funcionário ID: {}", id);
        if (!funcionarioRepository.existsById(id)) {
            log.warn("Funcionário ID: {} não encontrado para remoção.", id);
            throw new RecursoNaoEncontradoException("Funcionário não encontrado.");
        }
        // NOTA: Adicionar verificação se funcionário possui envios antes de excluir
        funcionarioRepository.deleteById(id);
        log.info("Funcionário ID: {} removido com sucesso. (LGPD: Remoção de Dados Pessoais)", id);
    }

    private FuncionarioResponse mapearParaResposta(FuncionarioEntity funcionarioEntity) {
        return new FuncionarioResponse(funcionarioEntity);
    }

    private String sanitizarTexto(String texto) {
        return Jsoup.clean(texto, Safelist.none()).trim();
    }

    private String sanitizarCelular(String celular) {
        String semTags = Jsoup.clean(celular, Safelist.none());
        return semTags.replaceAll("[^0-9()+\\-\\s]", "").trim();
    }
}
// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/FuncionarioServico.java
package br.com.ddsfacil.funcionario;

import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.funcionario.dto.FuncionarioRequisicao;
import br.com.ddsfacil.funcionario.dto.FuncionarioResposta;
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
public class FuncionarioServico {

    private static final Logger log = LoggerFactory.getLogger(FuncionarioServico.class);
    private final FuncionarioRepositorio funcionarioRepositorio;

    public FuncionarioServico(FuncionarioRepositorio funcionarioRepositorio) {
        this.funcionarioRepositorio = funcionarioRepositorio;
    }

    @Transactional
    public FuncionarioResposta criar(FuncionarioRequisicao requisicao) {
        Objects.requireNonNull(requisicao, "Requisição não pode ser nula.");

        String nomeLimpo = sanitizarTexto(requisicao.getNome());
        String celularLimpo = sanitizarCelular(requisicao.getCelular());
        String obraLimpa = sanitizarTexto(requisicao.getObra());

        log.info("Criando novo funcionário. Nome: {}, Obra: {}", nomeLimpo, obraLimpa);

        Funcionario funcionario = new Funcionario(nomeLimpo, celularLimpo, obraLimpa);
        Funcionario salvo = funcionarioRepositorio.save(funcionario);

        log.info("Funcionário criado com ID: {}. (LGPD: Dados Pessoais envolvidos)", salvo.getId());
        return mapearParaResposta(salvo);
    }

    @Transactional(readOnly = true)
    public List<FuncionarioResposta> listar(String obra) {
        List<Funcionario> funcionarios;
        if (obra != null && !obra.isBlank()) {
            String obraLimpa = sanitizarTexto(obra);
            funcionarios = funcionarioRepositorio.findByObraIgnoreCaseOrderByNomeAsc(obraLimpa);
        } else {
            funcionarios = funcionarioRepositorio.findAllByOrderByNomeAsc();
        }
        return funcionarios.stream().map(this::mapearParaResposta).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> listarObras() {
        // 3. REVERTER a chamada para usar o método da @Query
        return funcionarioRepositorio.listarObrasOrdenadas();
    }

    @Transactional
    public void remover(Long id) {
        log.info("Tentando remover funcionário ID: {}", id);
        if (!funcionarioRepositorio.existsById(id)) {
            log.warn("Funcionário ID: {} não encontrado para remoção.", id);
            throw new RecursoNaoEncontradoException("Funcionário não encontrado.");
        }
        funcionarioRepositorio.deleteById(id);
        log.info("Funcionário ID: {} removido com sucesso. (LGPD: Remoção de Dados Pessoais)", id);
    }

    private FuncionarioResposta mapearParaResposta(Funcionario funcionario) {
        return new FuncionarioResposta(funcionario.getId(), funcionario.getNome(), funcionario.getCelular(), funcionario.getObra());
    }

    private String sanitizarTexto(String texto) {
        return Jsoup.clean(texto, Safelist.none()).trim();
    }

    private String sanitizarCelular(String celular) {
        String semTags = Jsoup.clean(celular, Safelist.none());
        return semTags.replaceAll("[^0-9()+\\-\\s]", "").trim();
    }
}
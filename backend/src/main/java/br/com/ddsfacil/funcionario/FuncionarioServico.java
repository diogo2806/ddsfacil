package br.com.ddsfacil.funcionario;

import br.com.ddsfacil.funcionario.dto.FuncionarioRequisicao;
import br.com.ddsfacil.funcionario.dto.FuncionarioResposta;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FuncionarioServico {

    private final FuncionarioRepositorio funcionarioRepositorio;

    public FuncionarioServico(FuncionarioRepositorio funcionarioRepositorio) {
        this.funcionarioRepositorio = funcionarioRepositorio;
    }

    @Transactional
    public FuncionarioResposta criar(FuncionarioRequisicao requisicao) {
        String nomeLimpo = sanitizarTexto(requisicao.getNome());
        String celularLimpo = sanitizarCelular(requisicao.getCelular());
        String obraLimpa = sanitizarTexto(requisicao.getObra());

        Funcionario funcionario = new Funcionario(nomeLimpo, celularLimpo, obraLimpa);
        Funcionario salvo = funcionarioRepositorio.save(funcionario);
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
        return funcionarioRepositorio.listarObrasOrdenadas();
    }

    @Transactional
    public void remover(Long id) {
        if (!funcionarioRepositorio.existsById(id)) {
            throw new RecursoNaoEncontradoException("Funcionário não encontrado.");
        }
        funcionarioRepositorio.deleteById(id);
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

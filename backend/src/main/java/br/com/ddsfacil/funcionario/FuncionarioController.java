// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/FuncionarioControlador.java
package br.com.ddsfacil.funcionario;

import br.com.ddsfacil.funcionario.dto.FuncionarioRequisicao;
import br.com.ddsfacil.funcionario.dto.FuncionarioResposta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funcionarios")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Funcionários", description = "Gerenciamento de funcionários (trabalhadores)")
public class FuncionarioControlador {

    private final FuncionarioServico funcionarioServico;

    public FuncionarioControlador(FuncionarioServico funcionarioServico) {
        this.funcionarioServico = funcionarioServico;
    }

    @GetMapping
    @Operation(summary = "Lista funcionários, opcionalmente filtrando por obra")
    public List<FuncionarioResposta> listar(@RequestParam(name = "obra", required = false) String obra) {
        return funcionarioServico.listar(obra);
    }

    @GetMapping("/obras")
    @Operation(summary = "Lista os nomes de todas as obras distintas cadastradas")
    public List<String> listarObras() {
        return funcionarioServico.listarObras();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo funcionário")
    public FuncionarioResposta criar(@Valid @RequestBody FuncionarioRequisicao requisicao) {
        return funcionarioServico.criar(requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um funcionário pelo ID")
    public void remover(@PathVariable Long id) {
        funcionarioServico.remover(id);
    }
}
// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/FuncionarioController.java
package br.com.ddsfacil.funcionario;

import br.com.ddsfacil.funcionario.dto.FuncionarioRequest;
import br.com.ddsfacil.funcionario.dto.FuncionarioResponse;
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
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    @Operation(summary = "Lista funcionários, opcionalmente filtrando por ID do local de trabalho")
    // 1. Parâmetro atualizado de 'obra' para 'localId'
    public List<FuncionarioResponse> listar(@RequestParam(name = "localId", required = false) Long localTrabalhoId) {
        return funcionarioService.listar(localTrabalhoId);
    }

    // 2. Endpoint GET /obras REMOVIDO.
    // O frontend deve agora chamar GET /api/locais-trabalho e GET /api/tipos-local

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo funcionário")
    public FuncionarioResponse criar(@Valid @RequestBody FuncionarioRequest requisicao) {
        return funcionarioService.criar(requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um funcionário pelo ID")
    public void remover(@PathVariable Long id) {
        funcionarioService.remover(id);
    }
}
// Arquivo: backend/src/main/java/br/com/ddsfacil/local/api/LocalTrabalhoController.java
package br.com.ddsfacil.local.api;

import br.com.ddsfacil.local.application.LocalTrabalhoService;
import br.com.ddsfacil.local.infrastructure.dto.LocalTrabalhoRequest;
import br.com.ddsfacil.local.infrastructure.dto.LocalTrabalhoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locais-trabalho")
@Tag(name = "Locais de Trabalho", description = "Gerenciamento dos locais de trabalho (Obras, Escritórios, etc.)")
public class LocalTrabalhoController {

    private final LocalTrabalhoService localTrabalhoService;

    public LocalTrabalhoController(LocalTrabalhoService localTrabalhoService) {
        this.localTrabalhoService = localTrabalhoService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os locais de trabalho cadastrados")
    public List<LocalTrabalhoResponse> listar() {
        return localTrabalhoService.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo local de trabalho")
    public LocalTrabalhoResponse criar(@Valid @RequestBody LocalTrabalhoRequest requisicao) {
        return localTrabalhoService.criar(requisicao);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um local de trabalho existente")
    public LocalTrabalhoResponse atualizar(@PathVariable Long id, @Valid @RequestBody LocalTrabalhoRequest requisicao) {
        return localTrabalhoService.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um local de trabalho pelo ID")
    public void remover(@PathVariable Long id) {
        localTrabalhoService.remover(id);
    }
}
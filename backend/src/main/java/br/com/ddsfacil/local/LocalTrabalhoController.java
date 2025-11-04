// Arquivo: backend/src/main/java/br/com/ddsfacil/local/LocalTrabalhoController.java
package br.com.ddsfacil.local;

import br.com.ddsfacil.local.dto.LocalTrabalhoRequest;
import br.com.ddsfacil.local.dto.LocalTrabalhoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locais-trabalho")
@CrossOrigin(origins = "http://localhost:5173")
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um local de trabalho pelo ID")
    public void remover(@PathVariable Long id) {
        localTrabalhoService.remover(id);
    }
}
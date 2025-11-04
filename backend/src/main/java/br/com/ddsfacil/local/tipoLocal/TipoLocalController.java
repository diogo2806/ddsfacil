// Arquivo: backend/src/main/java/br/com/ddsfacil/local/tipoLocal/TipoLocalController.java
package br.com.ddsfacil.local.tipoLocal;

import br.com.ddsfacil.local.tipoLocal.dto.TipoLocalRequest;
import br.com.ddsfacil.local.tipoLocal.dto.TipoLocalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
// [REMOVIDO] import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tipos-local")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Locais de Trabalho", description = "Endpoints para Tipos de Local")
public class TipoLocalController {

    // [REATORADO] Injetando o Service ao invés do Repository
    private final TipoLocalService tipoLocalService;

    public TipoLocalController(TipoLocalService tipoLocalService) {
        this.tipoLocalService = tipoLocalService;
    }

    /**
     * Endpoint existente (para comboboxes do frontend)
     */
    @GetMapping
    @Operation(summary = "Lista os tipos de local (para combobox)", description = "Retorna uma lista de 'value' (ID) e 'label' (Nome) para selects.")
    public ResponseEntity<List<Map<String, Object>>> listarParaCombobox() {
        List<Map<String, Object>> lista = tipoLocalService.listarParaCombobox();
        return ResponseEntity.ok(lista);
    }

    /**
     * [NOVO] Endpoint de listagem para administração
     */
    @GetMapping("/admin")
    @Operation(summary = "Lista todos os tipos de local (para admin)", description = "Retorna a lista completa de tipos de local cadastrados.")
    public List<TipoLocalResponse> listarAdmin() {
        return tipoLocalService.listarTodosAdmin();
    }

    /**
     * [NOVO] Endpoint de criação
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo tipo de local")
    public TipoLocalResponse criar(@Valid @RequestBody TipoLocalRequest request) {
        return tipoLocalService.criar(request);
    }

    /**
     * [NOVO] Endpoint de atualização
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um tipo de local existente")
    public TipoLocalResponse atualizar(@PathVariable Long id, @Valid @RequestBody TipoLocalRequest request) {
        return tipoLocalService.atualizar(id, request);
    }

    /**
     * [NOVO] Endpoint de exclusão
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um tipo de local")
    public void remover(@PathVariable Long id) {
        tipoLocalService.remover(id);
    }
}
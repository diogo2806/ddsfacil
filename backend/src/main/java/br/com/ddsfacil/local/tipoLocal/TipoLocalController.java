// Arquivo: backend/src/main/java/br/com/ddsfacil/local/TipoLocalController.java
package br.com.ddsfacil.local;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tipos-local")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Locais de Trabalho", description = "Endpoints para Tipos de Local")
public class TipoLocalController {

    private final TipoLocalRepository tipoLocalRepository;

    public TipoLocalController(TipoLocalRepository tipoLocalRepository) {
        this.tipoLocalRepository = tipoLocalRepository;
    }

    @GetMapping
    @Operation(summary = "Lista os possíveis tipos de local de trabalho (Obra, Escritório, etc.)")
    public ResponseEntity<List<Map<String, Object>>> listarTipos() {
        List<Map<String, Object>> lista = tipoLocalRepository.findAllByOrderByNomeAsc().stream()
                .map(tipo -> Map.of(
                        "value", (Object) tipo.getId(),    // Ex: 1
                        "label", (Object) tipo.getNome() // Ex: "Obra"
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }
}
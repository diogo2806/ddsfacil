package br.com.ddsfacil.conteudo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conteudos/tipos")
@Tag(name = "Conteúdos DDS", description = "Endpoints relacionados aos tipos de conteúdo")
public class TipoConteudoControlador {

    @GetMapping
    @Operation(summary = "Lista os possíveis tipos de conteúdo")
    public ResponseEntity<List<Map<String, String>>> listarTipos() {
        List<Map<String, String>> lista = Arrays.stream(TipoConteudo.values())
                .map(tipo -> Map.of(
                        "value", tipo.name(), // Ex: "TEXTO"
                        "label", tipo.getDescricao() // Ex: "TEXTO"
                ))
                .toList();
        return ResponseEntity.ok(lista);
    }
}
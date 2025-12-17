// Arquivo: backend/src/main/java/br/com/ddsfacil/envio/api/EnvioDdsController.java
package br.com.ddsfacil.envio.api;

import br.com.ddsfacil.envio.application.EnvioDdsService;
import br.com.ddsfacil.envio.domain.StatusEnvioDds;
import br.com.ddsfacil.envio.infrastructure.dto.EnvioDdsRequest;
import br.com.ddsfacil.envio.infrastructure.dto.EnvioDdsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envios")
@Tag(name = "Envios DDS", description = "Gerenciamento e dashboard de envios de DDS")
public class EnvioDdsController {

    private final EnvioDdsService envioServico;

    public EnvioDdsController(EnvioDdsService envioServico) {
        this.envioServico = envioServico;
    }

    @GetMapping
    @Operation(summary = "Lista os envios de DDS por data (padrão: data atual)")
    public List<EnvioDdsResponse> listarPorData(
            @RequestParam(name = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data
    ) {
        return envioServico.listarPorData(data);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envia um DDS para uma lista de funcionários")
    public List<EnvioDdsResponse> enviar(@Valid @RequestBody EnvioDdsRequest requisicao) {
        return envioServico.enviar(requisicao);
    }

    @PostMapping("/{id}/confirmacoes")
    @Operation(summary = "Força a confirmação de um envio (simulação manual)")
    public EnvioDdsResponse confirmar(@PathVariable Long id) {
        return envioServico.confirmar(id);
    }

    @GetMapping("/status")
    @Operation(summary = "Lista os possíveis status de envio")
    public ResponseEntity<List<Map<String, String>>> listarStatus() {
        List<Map<String, String>> lista = Arrays.stream(StatusEnvioDds.values())
                .map(status -> Map.of(
                        "value", status.name(), // Ex: "ENVIADO"
                        "label", status.getDescricao() // Ex: "Enviado"
                ))
                .toList();
        return ResponseEntity.ok(lista);
    }
}
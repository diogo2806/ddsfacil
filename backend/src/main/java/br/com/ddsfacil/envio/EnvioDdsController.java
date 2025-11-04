// Arquivo: backend/src/main/java/br/com/ddsfacil/envio/EnvioDdsControlador.java
package br.com.ddsfacil.envio;

import br.com.ddsfacil.envio.dto.EnvioDdsRequisicao;
import br.com.ddsfacil.envio.dto.EnvioDdsResposta;
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
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Envios DDS", description = "Gerenciamento e dashboard de envios de DDS")
public class EnvioDdsControlador {

    private final EnvioDdsServico envioServico;

    public EnvioDdsControlador(EnvioDdsServico envioServico) {
        this.envioServico = envioServico;
    }

    @GetMapping
    @Operation(summary = "Lista os envios de DDS por data (padrão: data atual)")
    public List<EnvioDdsResposta> listarPorData(
            @RequestParam(name = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data
    ) {
        return envioServico.listarPorData(data);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envia um DDS para uma lista de funcionários")
    public List<EnvioDdsResposta> enviar(@Valid @RequestBody EnvioDdsRequisicao requisicao) {
        return envioServico.enviar(requisicao);
    }

    @PostMapping("/{id}/confirmacoes")
    @Operation(summary = "Força a confirmação de um envio (simulação manual)")
    public EnvioDdsResposta confirmar(@PathVariable Long id) {
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
// Arquivo: backend/src/main/java/br/com/ddsfacil/confirmacaoTrabalhador/ConfirmacaoPublicaController.java
package br.com.ddsfacil.confirmacaoTrabalhador;

import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorConfirmacaoResponse;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/dds")
@CrossOrigin(origins = "*")
@Tag(name = "Confirmação Pública", description = "Endpoints públicos para confirmação do trabalhador via token")
public class ConfirmacaoPublicaController {

    private final ConfirmacaoTrabalhadorService confirmacaoServico;

    public ConfirmacaoPublicaController(ConfirmacaoTrabalhadorService confirmacaoServico) {
        this.confirmacaoServico = confirmacaoServico;
    }

    @GetMapping("/{tokenAcesso}")
    @Operation(summary = "Busca o conteúdo de um DDS por token de acesso")
    public ConfirmacaoTrabalhadorResponse buscarPorToken(@PathVariable String tokenAcesso) {
        return confirmacaoServico.buscarPorToken(tokenAcesso);
    }

    @PostMapping("/{tokenAcesso}/confirmar")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Confirma a leitura de um DDS por token de acesso")
    public ConfirmacaoTrabalhadorConfirmacaoResponse confirmar(@PathVariable String tokenAcesso) {
        return confirmacaoServico.confirmarPorToken(tokenAcesso, LocalDateTime.now());
    }
}
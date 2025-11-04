// Arquivo: backend/src/main/java/br/com/ddsfacil/confirmacaoTrabalhador/ConfirmacaoPublicaControlador.java
package br.com.ddsfacil.confirmacaoTrabalhador;

import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorConfirmacaoResposta;
import br.com.ddsfacil.confirmacaoTrabalhador.dto.ConfirmacaoTrabalhadorResposta;
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
public class ConfirmacaoPublicaControlador {

    private final ConfirmacaoTrabalhadorServico confirmacaoServico;

    public ConfirmacaoPublicaControlador(ConfirmacaoTrabalhadorServico confirmacaoServico) {
        this.confirmacaoServico = confirmacaoServico;
    }

    @GetMapping("/{tokenAcesso}")
    @Operation(summary = "Busca o conteúdo de um DDS por token de acesso")
    public ConfirmacaoTrabalhadorResposta buscarPorToken(@PathVariable String tokenAcesso) {
        return confirmacaoServico.buscarPorToken(tokenAcesso);
    }

    @PostMapping("/{tokenAcesso}/confirmar")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Confirma a leitura de um DDS por token de acesso")
    public ConfirmacaoTrabalhadorConfirmacaoResposta confirmar(@PathVariable String tokenAcesso) {
        return confirmacaoServico.confirmarPorToken(tokenAcesso, LocalDateTime.now());
    }
}
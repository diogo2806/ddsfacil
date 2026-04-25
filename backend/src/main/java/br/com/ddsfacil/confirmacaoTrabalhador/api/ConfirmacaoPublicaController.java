// Arquivo: backend/src/main/java/br/com/ddsfacil/confirmacaoTrabalhador/api/ConfirmacaoPublicaController.java
package br.com.ddsfacil.confirmacaoTrabalhador.api;

import br.com.ddsfacil.confirmacaoTrabalhador.application.ConfirmacaoTrabalhadorService;
import br.com.ddsfacil.confirmacaoTrabalhador.infrastructure.dto.ConfirmacaoTrabalhadorConfirmacaoResponse;
import br.com.ddsfacil.confirmacaoTrabalhador.infrastructure.dto.ConfirmacaoTrabalhadorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.CacheControl;


@RestController
@RequestMapping("/api/public/dds")
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

    @GetMapping("/{tokenAcesso}/arquivo")
    @Operation(summary = "Realiza o download seguro do arquivo associado ao DDS")
    public ResponseEntity<ByteArrayResource> baixarArquivo(@PathVariable String tokenAcesso) {
        var arquivo = confirmacaoServico.buscarArquivoPorToken(tokenAcesso);
        ByteArrayResource recurso = new ByteArrayResource(arquivo.getDados());

        return ResponseEntity
            .ok()
            // 1. Desabilita o Cache para forçar o download novo
            .cacheControl(CacheControl.noCache().mustRevalidate()) 
            // 2. Informa o tamanho exato (Corrige o erro de renderização do PDF)
            .contentLength(arquivo.getDados().length) 
            .contentType(arquivo.getTipoMidia())
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + arquivo.getNomeArquivo() + "\"")
            .body(recurso);
    }
}
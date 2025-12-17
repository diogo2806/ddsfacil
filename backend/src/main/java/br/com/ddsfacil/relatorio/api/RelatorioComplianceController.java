package br.com.ddsfacil.relatorio.api;

import br.com.ddsfacil.relatorio.application.RelatorioComplianceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios/dds")
@Tag(name = "Relatórios de Compliance", description = "Geração de PDF de evidência de DDS")
public class RelatorioComplianceController {

    private final RelatorioComplianceService relatorioComplianceService;

    public RelatorioComplianceController(RelatorioComplianceService relatorioComplianceService) {
        this.relatorioComplianceService = relatorioComplianceService;
    }

    @GetMapping("/compliance")
    @Operation(summary = "Gera o PDF de evidência de DDS consolidado")
    public ResponseEntity<byte[]> gerarRelatorio(
            @RequestParam("conteudoId") Long conteudoId,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(value = "localTrabalhoId", required = false) Long localTrabalhoId
    ) {
        byte[] pdf = relatorioComplianceService.gerarPdfEvidencia(conteudoId, data, localTrabalhoId);

        String nomeArquivo = String.format("evidencia_dds_%s.pdf", data);
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setContentType(MediaType.APPLICATION_PDF);
        cabecalhos.setContentDisposition(ContentDisposition.attachment().filename(nomeArquivo).build());

        return new ResponseEntity<>(pdf, cabecalhos, HttpStatus.OK);
    }
}

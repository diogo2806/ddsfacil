package br.com.ddsfacil.licenca;

import br.com.ddsfacil.configuracao.multitenant.ContextoEmpresa;
import br.com.ddsfacil.licenca.dto.SaldoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/licencas")
@Tag(name = "Licenças e Cotas", description = "Gerenciamento de saldo e planos")
public class LicencaController {

    private final LicencaService licencaService;

    public LicencaController(LicencaService licencaService) {
        this.licencaService = licencaService;
    }

    @GetMapping("/meu-saldo")
    @Operation(summary = "Consulta o saldo atual de SMS da empresa logada")
    public ResponseEntity<SaldoResponse> consultarSaldo() {
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();
        return ResponseEntity.ok(licencaService.consultarSaldo(empresaId));
    }
}
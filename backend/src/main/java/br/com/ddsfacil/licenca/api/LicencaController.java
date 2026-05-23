package br.com.ddsfacil.licenca.api;

import br.com.ddsfacil.configuracao.multitenant.ContextoEmpresa;
import br.com.ddsfacil.envio.domain.CanalMensagem;
import br.com.ddsfacil.licenca.application.LicencaService;
import br.com.ddsfacil.licenca.infrastructure.dto.AtualizarLicencaRequest;
import br.com.ddsfacil.licenca.infrastructure.dto.LicencaResponse;
import br.com.ddsfacil.licenca.infrastructure.dto.RecargaOnlineResponse;
import br.com.ddsfacil.licenca.infrastructure.dto.RecargaRequest;
import br.com.ddsfacil.licenca.infrastructure.dto.SaldoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Detalha a licença da empresa logada (plano, saldo, status de pagamento)")
    public ResponseEntity<LicencaResponse> detalhar() {
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();
        return ResponseEntity.ok(licencaService.detalhar(empresaId));
    }

    @PostMapping("/recarga")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Recarga manual de créditos de SMS (gerenciada pelo administrador)")
    public ResponseEntity<LicencaResponse> recarregar(@Valid @RequestBody RecargaRequest requisicao) {
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();
        CanalMensagem canal = requisicao.getCanal() == null ? CanalMensagem.SMS : requisicao.getCanal();
        return ResponseEntity.ok(licencaService.recarregarManual(empresaId, canal, requisicao.getQuantidade()));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza plano, status de pagamento e data de renovação da licença")
    public ResponseEntity<LicencaResponse> atualizar(@Valid @RequestBody AtualizarLicencaRequest requisicao) {
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();
        return ResponseEntity.ok(licencaService.atualizar(empresaId, requisicao));
    }

    @PostMapping("/recarga-online")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inicia uma recarga via gateway de pagamento (stub enquanto não há provedor)")
    public ResponseEntity<RecargaOnlineResponse> recargaOnline(@Valid @RequestBody RecargaRequest requisicao) {
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();
        return ResponseEntity.ok(licencaService.iniciarRecargaOnline(empresaId, requisicao.getQuantidade()));
    }
}

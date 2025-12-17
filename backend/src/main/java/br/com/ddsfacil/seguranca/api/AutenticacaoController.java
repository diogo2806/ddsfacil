package br.com.ddsfacil.seguranca.api;

import br.com.ddsfacil.seguranca.application.AutenticacaoService;
import br.com.ddsfacil.seguranca.infrastructure.dto.AutenticacaoRequest;
import br.com.ddsfacil.seguranca.infrastructure.dto.AutenticacaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/autenticacao")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    public ResponseEntity<AutenticacaoResponse> autenticar(@Valid @RequestBody AutenticacaoRequest request) {
        return ResponseEntity.ok(autenticacaoService.autenticar(request));
    }
}

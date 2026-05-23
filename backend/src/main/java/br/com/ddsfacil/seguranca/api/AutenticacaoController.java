package br.com.ddsfacil.seguranca.api;

import br.com.ddsfacil.seguranca.application.AutenticacaoService;
import br.com.ddsfacil.seguranca.infrastructure.dto.AutenticacaoRequest;
import br.com.ddsfacil.seguranca.infrastructure.dto.AutenticacaoResponse;
import br.com.ddsfacil.seguranca.recuperacao.application.RecuperacaoSenhaService;
import br.com.ddsfacil.seguranca.recuperacao.infrastructure.dto.RedefinirComTokenRequest;
import br.com.ddsfacil.seguranca.recuperacao.infrastructure.dto.SolicitarRedefinicaoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/autenticacao")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;

    public AutenticacaoController(
            AutenticacaoService autenticacaoService,
            RecuperacaoSenhaService recuperacaoSenhaService
    ) {
        this.autenticacaoService = autenticacaoService;
        this.recuperacaoSenhaService = recuperacaoSenhaService;
    }

    @PostMapping("/login")
    public ResponseEntity<AutenticacaoResponse> autenticar(@Valid @RequestBody AutenticacaoRequest request) {
        return ResponseEntity.ok(autenticacaoService.autenticar(request));
    }

    @PostMapping("/esqueci-senha")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void esqueciSenha(@Valid @RequestBody SolicitarRedefinicaoRequest request) {
        recuperacaoSenhaService.solicitar(request.getEmail());
    }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(@Valid @RequestBody RedefinirComTokenRequest request) {
        recuperacaoSenhaService.redefinir(request.getToken(), request.getNovaSenha());
    }
}

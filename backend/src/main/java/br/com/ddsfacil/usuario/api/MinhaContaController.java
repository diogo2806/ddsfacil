package br.com.ddsfacil.usuario.api;

import br.com.ddsfacil.seguranca.infrastructure.UsuarioAutenticado;
import br.com.ddsfacil.usuario.application.UsuarioService;
import br.com.ddsfacil.usuario.infrastructure.dto.AlterarSenhaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/minha-conta")
@Tag(name = "Minha Conta", description = "Operações do usuário autenticado sobre a própria conta")
public class MinhaContaController {

    private final UsuarioService usuarioService;

    public MinhaContaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PutMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Altera a senha do usuário autenticado")
    public void alterarSenha(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody AlterarSenhaRequest requisicao
    ) {
        usuarioService.alterarMinhaSenha(usuario.getUsuarioId(), requisicao);
    }
}

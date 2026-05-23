package br.com.ddsfacil.usuario.api;

import br.com.ddsfacil.usuario.application.UsuarioService;
import br.com.ddsfacil.usuario.infrastructure.dto.RedefinirSenhaRequest;
import br.com.ddsfacil.usuario.infrastructure.dto.UsuarioAtualizacaoRequest;
import br.com.ddsfacil.usuario.infrastructure.dto.UsuarioRequest;
import br.com.ddsfacil.usuario.infrastructure.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuários", description = "Gestão de usuários do painel (somente ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Lista os usuários da empresa")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo usuário do painel")
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest requisicao) {
        return usuarioService.criar(requisicao);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome, perfil e status de um usuário")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioAtualizacaoRequest requisicao) {
        return usuarioService.atualizar(id, requisicao);
    }

    @PutMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Redefine a senha de um usuário")
    public void redefinirSenha(@PathVariable Long id, @Valid @RequestBody RedefinirSenhaRequest requisicao) {
        usuarioService.redefinirSenha(id, requisicao);
    }
}

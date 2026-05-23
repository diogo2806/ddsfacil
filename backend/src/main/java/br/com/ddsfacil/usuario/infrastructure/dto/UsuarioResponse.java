package br.com.ddsfacil.usuario.infrastructure.dto;

import br.com.ddsfacil.usuario.domain.PerfilUsuario;
import br.com.ddsfacil.usuario.domain.UsuarioEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;

@Schema(description = "DTO de resposta para um usuário do painel")
@Getter
public class UsuarioResponse {

    private final Long id;
    private final String nome;
    private final String email;
    private final PerfilUsuario perfil;
    private final boolean ativo;
    private final LocalDateTime criadoEm;

    public UsuarioResponse(UsuarioEntity usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.perfil = usuario.getPerfil();
        this.ativo = usuario.isAtivo();
        this.criadoEm = usuario.getCriadoEm();
    }
}

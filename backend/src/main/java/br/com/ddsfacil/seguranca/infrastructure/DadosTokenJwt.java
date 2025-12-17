package br.com.ddsfacil.seguranca.infrastructure;

import br.com.ddsfacil.usuario.domain.PerfilUsuario;

public record DadosTokenJwt(Long usuarioId, Long empresaId, String email, PerfilUsuario perfil, String nome) {
}

package br.com.ddsfacil.seguranca;

import br.com.ddsfacil.usuario.PerfilUsuario;

public record DadosTokenJwt(Long usuarioId, Long empresaId, String email, PerfilUsuario perfil, String nome) {
}

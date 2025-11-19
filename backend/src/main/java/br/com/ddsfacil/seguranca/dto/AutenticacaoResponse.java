package br.com.ddsfacil.seguranca.dto;

import br.com.ddsfacil.usuario.PerfilUsuario;

public class AutenticacaoResponse {

    private final String token;
    private final Long empresaId;
    private final String nomeUsuario;
    private final PerfilUsuario perfil;

    public AutenticacaoResponse(String token, Long empresaId, String nomeUsuario, PerfilUsuario perfil) {
        this.token = token;
        this.empresaId = empresaId;
        this.nomeUsuario = nomeUsuario;
        this.perfil = perfil;
    }

    public String getToken() {
        return token;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }
}

package br.com.ddsfacil.seguranca.infrastructure;

import br.com.ddsfacil.usuario.domain.PerfilUsuario;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioAutenticado implements UserDetails {

    private final Long usuarioId;
    private final Long empresaId;
    private final String email;
    private final String nome;
    private final PerfilUsuario perfil;
    private final List<GrantedAuthority> autoridades;

    public UsuarioAutenticado(DadosTokenJwt dadosTokenJwt) {
        this.usuarioId = dadosTokenJwt.usuarioId();
        this.empresaId = dadosTokenJwt.empresaId();
        this.email = dadosTokenJwt.email();
        this.nome = dadosTokenJwt.nome();
        this.perfil = dadosTokenJwt.perfil();
        this.autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + this.perfil.name()));
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getNome() {
        return nome;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return autoridades;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

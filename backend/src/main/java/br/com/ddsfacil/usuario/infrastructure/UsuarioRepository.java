package br.com.ddsfacil.usuario.infrastructure;

import br.com.ddsfacil.usuario.domain.UsuarioEntity;
import br.com.ddsfacil.usuario.domain.PerfilUsuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    @EntityGraph(attributePaths = "empresa")
    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "empresa")
    Optional<UsuarioEntity> findFirstByPerfilAndEmpresaId(PerfilUsuario perfil, Long empresaId);
}

package br.com.ddsfacil.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    @EntityGraph(attributePaths = "empresa")
    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);
}

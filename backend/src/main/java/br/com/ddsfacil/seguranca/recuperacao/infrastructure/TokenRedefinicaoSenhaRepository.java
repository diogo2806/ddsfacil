package br.com.ddsfacil.seguranca.recuperacao.infrastructure;

import br.com.ddsfacil.seguranca.recuperacao.domain.TokenRedefinicaoSenha;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha, Long> {

    @EntityGraph(attributePaths = "usuario")
    Optional<TokenRedefinicaoSenha> findByToken(String token);
}

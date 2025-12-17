// Arquivo: backend/src/main/java/br/com/ddsfacil/local/infrastructure/LocalTrabalhoRepository.java
package br.com.ddsfacil.local.infrastructure;

import br.com.ddsfacil.local.domain.LocalTrabalho;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocalTrabalhoRepository extends JpaRepository<LocalTrabalho, Long> {

    @EntityGraph(value = "LocalTrabalho.withTipoLocal")
    List<LocalTrabalho> findAllByOrderByNomeAsc();

    boolean existsByTipoLocalId(Long tipoLocalId);
}
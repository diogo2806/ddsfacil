// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/infrastructure/FuncionarioRepository.java
package br.com.ddsfacil.funcionario.infrastructure;

import br.com.ddsfacil.funcionario.domain.FuncionarioEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query; // [REMOVIDO]

public interface FuncionarioRepository extends JpaRepository<FuncionarioEntity, Long> {

    /**
     * [REATORADO] Substituída @Query por método derivado com @EntityGraph.
     * (Regra 2.5 - Sem @Query)
     */
    @EntityGraph(value = "Funcionario.withLocalTrabalhoAndTipo")
    List<FuncionarioEntity> findAllByLocalTrabalhoIdOrderByNomeAsc(Long localTrabalhoId);

    /**
     * [REATORADO] Substituída @Query por método derivado com @EntityGraph.
     * (Regra 2.5 - Sem @Query)
     */
    @EntityGraph(value = "Funcionario.withLocalTrabalhoAndTipo")
    List<FuncionarioEntity> findAllByOrderByNomeAsc();
}
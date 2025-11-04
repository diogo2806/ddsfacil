// Arquivo: backend/src/main/java/br/com/ddsfacil/local/tipoLocal/TipoLocalRepository.java
package br.com.ddsfacil.local.tipoLocal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional; // [NOVO] Importado

public interface TipoLocalRepository extends JpaRepository<TipoLocal, Long> {

    List<TipoLocal> findAllByOrderByNomeAsc();

    Optional<TipoLocal> findByNomeIgnoreCase(String nome);
}
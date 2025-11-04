// Arquivo: backend/src/main/java/br/com/ddsfacil/local/TipoLocalRepository.java
package br.com.ddsfacil.local;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TipoLocalRepository extends JpaRepository<TipoLocal, Long> {

    List<TipoLocal> findAllByOrderByNomeAsc();
}
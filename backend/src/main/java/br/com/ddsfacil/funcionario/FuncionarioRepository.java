// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/FuncionarioRepositorio.java
package br.com.ddsfacil.funcionario;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 1. Adicionar este import

public interface FuncionarioRepositorio extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findByObraIgnoreCaseOrderByNomeAsc(String obra);

    List<Funcionario> findAllByOrderByNomeAsc();

    // 2. REVERTER este método para a @Query original
    @Query("select distinct f.obra from Funcionario f order by f.obra asc")
    List<String> listarObrasOrdenadas();
}
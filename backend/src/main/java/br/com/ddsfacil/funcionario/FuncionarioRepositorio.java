package br.com.ddsfacil.funcionario;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FuncionarioRepositorio extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findByObraIgnoreCaseOrderByNomeAsc(String obra);

    List<Funcionario> findAllByOrderByNomeAsc();

    @Query("select distinct f.obra from Funcionario f order by f.obra asc")
    List<String> listarObrasOrdenadas();
}

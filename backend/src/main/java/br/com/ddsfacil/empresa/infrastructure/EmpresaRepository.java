package br.com.ddsfacil.empresa.infrastructure;

import br.com.ddsfacil.empresa.domain.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<EmpresaEntity, Long> {
}

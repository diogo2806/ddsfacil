package br.com.ddsfacil.licenca;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface LicencaRepository extends JpaRepository<LicencaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select licenca from LicencaEntity licenca where licenca.empresa.id = :empresaId")
    Optional<LicencaEntity> buscarPorEmpresaIdParaAtualizacao(@Param("empresaId") Long empresaId);

    @Query("select licenca from LicencaEntity licenca where licenca.empresa.id = :empresaId")
    Optional<LicencaEntity> buscarPorEmpresaId(@Param("empresaId") Long empresaId);
}

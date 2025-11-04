// Arquivo: backend/src/main/java/br/com/ddsfacil/envio/EnvioDdsRepository.java
package br.com.ddsfacil.envio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// ...
public interface EnvioDdsRepository extends JpaRepository<EnvioDdsEntity, Long> {

    List<EnvioDdsEntity> findByDataEnvioOrderByMomentoEnvioAsc(LocalDate dataEnvio);

    // CORREÇÃO: Renomear de 'FuncionarioId' para 'FuncionarioEntityId'
    Optional<EnvioDdsEntity> findByDataEnvioAndFuncionarioEntityIdAndConteudoId(
            LocalDate dataEnvio,
            Long funcionarioId,
            Long conteudoId
    );

    Optional<EnvioDdsEntity> findByTokenAcesso(String tokenAcesso);
}
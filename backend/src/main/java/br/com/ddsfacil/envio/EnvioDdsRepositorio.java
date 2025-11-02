package br.com.ddsfacil.envio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvioDdsRepositorio extends JpaRepository<EnvioDds, Long> {

    List<EnvioDds> findByDataEnvioOrderByMomentoEnvioAsc(LocalDate dataEnvio);

    Optional<EnvioDds> findByDataEnvioAndFuncionarioIdAndConteudoId(LocalDate dataEnvio, Long funcionarioId, Long conteudoId);
}

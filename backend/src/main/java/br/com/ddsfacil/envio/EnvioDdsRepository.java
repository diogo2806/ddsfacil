package br.com.ddsfacil.envio;

import br.com.ddsfacil.envio.StatusEnvioDds;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnvioDdsRepository extends JpaRepository<EnvioDdsEntity, Long> {

    List<EnvioDdsEntity> findByDataEnvioOrderByMomentoEnvioAsc(LocalDate dataEnvio);

    @Query("""
            select envio from EnvioDdsEntity envio
            join fetch envio.funcionarioEntity funcionario
            join fetch funcionario.localTrabalho local
            where envio.dataEnvio = :dataEnvio
              and envio.conteudo.id = :conteudoId
              and envio.status = :status
              and (:localTrabalhoId is null or local.id = :localTrabalhoId)
            order by local.nome asc, funcionario.nome asc, envio.momentoEnvio asc
            """)
    List<EnvioDdsEntity> buscarConfirmadosPorDataConteudoStatusELocal(
            @Param("dataEnvio") LocalDate dataEnvio,
            @Param("conteudoId") Long conteudoId,
            @Param("status") StatusEnvioDds status,
            @Param("localTrabalhoId") Long localTrabalhoId
    );

    Optional<EnvioDdsEntity> findByDataEnvioAndFuncionarioEntityIdAndConteudoId(
            LocalDate dataEnvio,
            Long funcionarioId,
            Long conteudoId
    );

    Optional<EnvioDdsEntity> findByTokenAcesso(String tokenAcesso);

    // --- ADICIONAR ESTE MÉTODO ---
    boolean existsByConteudoId(Long conteudoId);
}
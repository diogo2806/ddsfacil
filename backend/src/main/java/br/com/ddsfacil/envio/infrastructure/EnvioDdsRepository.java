package br.com.ddsfacil.envio.infrastructure;

import br.com.ddsfacil.envio.domain.EnvioDdsEntity;
import br.com.ddsfacil.envio.domain.StatusEnvioDds;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Query("""
            select envio.id from EnvioDdsEntity envio
            where envio.status = :status
              and envio.quantidadeLembretes < :maximo
              and coalesce(envio.momentoUltimoLembrete, envio.momentoEnvio) <= :limite
            """)
    List<Long> buscarIdsParaLembrete(
            @Param("status") StatusEnvioDds status,
            @Param("maximo") int maximo,
            @Param("limite") LocalDateTime limite
    );

    // --- ADICIONAR ESTE MÉTODO ---
    boolean existsByConteudoId(Long conteudoId);

    boolean existsByFuncionarioEntityId(Long funcionarioId);
}
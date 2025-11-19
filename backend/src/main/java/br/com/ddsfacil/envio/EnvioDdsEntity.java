// Arquivo: backend/src/main/java/br/com/ddsfacil/envio/EnvioDdsEntity.java
package br.com.ddsfacil.envio;

import br.com.ddsfacil.conteudo.ConteudoDdsEntity;
import br.com.ddsfacil.funcionario.FuncionarioEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "envios_dds", indexes = @Index(name = "idx_envios_dds_empresa_id", columnList = "empresa_id"))
@Filter(name = "filtroEmpresa", condition = "empresa_id = :empresaId")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnvioDdsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FuncionarioEntity funcionarioEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conteudo_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ConteudoDdsEntity conteudo;

    @Column(name = "data_envio", nullable = false)
    private LocalDate dataEnvio;

    @Column(name = "momento_envio", nullable = false)
    private LocalDateTime momentoEnvio;

    @Column(name = "momento_confirmacao")
    private LocalDateTime momentoConfirmacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEnvioDds status;

    @Column(name = "token_acesso", nullable = false, unique = true, length = 64)
    private String tokenAcesso;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    public EnvioDdsEntity(
            FuncionarioEntity funcionarioEntity,
            ConteudoDdsEntity conteudo,
            LocalDate dataEnvio,
            LocalDateTime momentoEnvio,
            Long empresaId
    ) {
        this.funcionarioEntity = funcionarioEntity;
        this.conteudo = conteudo;
        this.dataEnvio = dataEnvio;
        this.momentoEnvio = momentoEnvio;
        this.status = StatusEnvioDds.PENDENTE;
        this.tokenAcesso = gerarToken();
        this.empresaId = empresaId;
    }

    public void confirmar(LocalDateTime momento) {
        if (this.status == StatusEnvioDds.CONFIRMADO) {
            return;
        }
        this.status = StatusEnvioDds.CONFIRMADO;
        this.momentoConfirmacao = momento;
    }

    public void marcarComoEnviado(LocalDateTime momento) {
        if (this.status == StatusEnvioDds.CONFIRMADO) {
            return;
        }
        this.status = StatusEnvioDds.ENVIADO;
        this.momentoEnvio = momento;
    }

    @PrePersist
    void prepararPersistencia() {
        if (this.tokenAcesso == null || this.tokenAcesso.isBlank()) {
            this.tokenAcesso = gerarToken();
        }
    }

    private String gerarToken() {
        return UUID.randomUUID().toString();
    }
}
package br.com.ddsfacil.envio;

import br.com.ddsfacil.conteudo.ConteudoDds;
import br.com.ddsfacil.funcionario.Funcionario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "envios_dds")
public class EnvioDds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conteudo_id")
    private ConteudoDds conteudo;

    @Column(name = "data_envio", nullable = false)
    private LocalDate dataEnvio;

    @Column(name = "momento_envio", nullable = false)
    private LocalDateTime momentoEnvio;

    @Column(name = "momento_confirmacao")
    private LocalDateTime momentoConfirmacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEnvioDds status;

    @Column(name = "token_acesso", nullable = false, unique = true, length = 64)
    private String tokenAcesso;

    protected EnvioDds() {}

    public EnvioDds(Funcionario funcionario, ConteudoDds conteudo, LocalDate dataEnvio, LocalDateTime momentoEnvio) {
        this.funcionario = funcionario;
        this.conteudo = conteudo;
        this.dataEnvio = dataEnvio;
        this.momentoEnvio = momentoEnvio;
        this.status = StatusEnvioDds.ENVIADO;
        this.tokenAcesso = gerarToken();
    }

    public Long getId() {
        return id;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public ConteudoDds getConteudo() {
        return conteudo;
    }

    public LocalDate getDataEnvio() {
        return dataEnvio;
    }

    public LocalDateTime getMomentoEnvio() {
        return momentoEnvio;
    }

    public LocalDateTime getMomentoConfirmacao() {
        return momentoConfirmacao;
    }

    public StatusEnvioDds getStatus() {
        return status;
    }

    public String getTokenAcesso() {
        return tokenAcesso;
    }

    public void confirmar(LocalDateTime momento) {
        if (this.status == StatusEnvioDds.CONFIRMADO) {
            return;
        }
        this.status = StatusEnvioDds.CONFIRMADO;
        this.momentoConfirmacao = momento;
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

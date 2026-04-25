package br.com.ddsfacil.licenca.domain;

import br.com.ddsfacil.empresa.domain.EmpresaEntity;
import br.com.ddsfacil.excecao.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "licencas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LicencaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false, unique = true)
    private EmpresaEntity empresa;

    @Column(name = "tipo_plano", nullable = false, length = 40)
    private String tipoPlano;

    @Column(name = "saldo_sms", nullable = false)
    private Integer saldoSms;

    @Column(name = "saldo_whatsapp", nullable = false)
    private Integer saldoWhatsapp;

    @Column(name = "data_renovacao")
    private LocalDate dataRenovacao;

    @Column(name = "status_pagamento", nullable = false, length = 20)
    private String statusPagamento;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public void consumirSaldoSms(int quantidade) {
        if (quantidade <= 0) {
            return;
        }
        if (saldoSms == null) {
            saldoSms = 0;
        }
        if (saldoSms < quantidade) {
            throw new RegraNegocioException("Saldo de SMS insuficiente. Contate o suporte para contratar mais créditos.");
        }
        saldoSms -= quantidade;
    }
}

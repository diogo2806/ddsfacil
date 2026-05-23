package br.com.ddsfacil.licenca.infrastructure.dto;

import br.com.ddsfacil.licenca.domain.LicencaEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LicencaResponse(
        String tipoPlano,
        Integer saldoSms,
        Integer saldoWhatsapp,
        LocalDate dataRenovacao,
        String statusPagamento,
        LocalDateTime atualizadoEm
) {
    public static LicencaResponse de(LicencaEntity licenca) {
        return new LicencaResponse(
                licenca.getTipoPlano(),
                licenca.getSaldoSms(),
                licenca.getSaldoWhatsapp(),
                licenca.getDataRenovacao(),
                licenca.getStatusPagamento(),
                licenca.getAtualizadoEm()
        );
    }
}

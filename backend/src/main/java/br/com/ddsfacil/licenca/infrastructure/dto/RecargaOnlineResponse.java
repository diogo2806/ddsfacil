package br.com.ddsfacil.licenca.infrastructure.dto;

public record RecargaOnlineResponse(
        String status,
        String mensagem,
        String urlPagamento
) {
}

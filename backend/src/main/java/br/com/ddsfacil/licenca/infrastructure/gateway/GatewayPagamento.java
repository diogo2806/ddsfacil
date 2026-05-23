package br.com.ddsfacil.licenca.infrastructure.gateway;

import br.com.ddsfacil.licenca.infrastructure.dto.RecargaOnlineResponse;

/**
 * Ponto de integração para um provedor de pagamento online (ex.: Mercado Pago, Stripe).
 * A implementação atual é um stub; ao plugar um gateway real, retornar a URL/checkout
 * de pagamento e, via webhook, creditar o saldo após a confirmação.
 */
public interface GatewayPagamento {

    RecargaOnlineResponse iniciarRecarga(Long empresaId, int quantidadeCreditos);
}

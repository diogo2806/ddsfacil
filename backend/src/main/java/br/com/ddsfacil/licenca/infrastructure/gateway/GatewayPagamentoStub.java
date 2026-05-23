package br.com.ddsfacil.licenca.infrastructure.gateway;

import br.com.ddsfacil.licenca.infrastructure.dto.RecargaOnlineResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementação placeholder enquanto não há um provedor de pagamento configurado.
 * Mantém o fluxo de recarga online disponível na API, sinalizando indisponibilidade
 * em vez de falhar, e registra a intenção de recarga para acompanhamento.
 */
@Service
public class GatewayPagamentoStub implements GatewayPagamento {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayPagamentoStub.class);

    @Override
    public RecargaOnlineResponse iniciarRecarga(Long empresaId, int quantidadeCreditos) {
        LOGGER.info("Recarga online solicitada pela empresa {} ({} créditos), mas nenhum gateway está configurado.",
                empresaId, quantidadeCreditos);
        return new RecargaOnlineResponse(
                "INDISPONIVEL",
                "Pagamento online ainda não está disponível. Utilize a recarga manual pelo administrador.",
                null
        );
    }
}

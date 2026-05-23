package br.com.ddsfacil.licenca.application;

import br.com.ddsfacil.excecao.RegraNegocioException;
import br.com.ddsfacil.licenca.domain.LicencaEntity;
import br.com.ddsfacil.licenca.infrastructure.LicencaRepository;
import br.com.ddsfacil.licenca.infrastructure.dto.AtualizarLicencaRequest;
import br.com.ddsfacil.licenca.infrastructure.dto.LicencaResponse;
import br.com.ddsfacil.licenca.infrastructure.dto.RecargaOnlineResponse;
import br.com.ddsfacil.licenca.infrastructure.dto.SaldoResponse;
import br.com.ddsfacil.licenca.infrastructure.gateway.GatewayPagamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LicencaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicencaService.class);
    private final LicencaRepository licencaRepository;
    private final GatewayPagamento gatewayPagamento;

    public LicencaService(LicencaRepository licencaRepository, GatewayPagamento gatewayPagamento) {
        this.licencaRepository = licencaRepository;
        this.gatewayPagamento = gatewayPagamento;
    }

    @Transactional
    public void debitarSms(Long empresaId, int quantidade) {
        LicencaEntity licenca = licencaRepository.buscarPorEmpresaIdParaAtualizacao(empresaId)
                .orElseThrow(() -> new RegraNegocioException("Licença não encontrada para a empresa informada."));
        if (!licenca.pagamentoEmDia()) {
            throw new RegraNegocioException("Pagamento pendente. Regularize a licença para enviar novos DDS.");
        }
        licenca.consumirSaldoSms(quantidade);
        LOGGER.info("Saldo de SMS da empresa {} debitado em {} crédito(s).", empresaId, quantidade);
    }

    @Transactional(readOnly = true)
    public boolean possuiCreditoParaEnvio(Long empresaId) {
        return licencaRepository.buscarPorEmpresaId(empresaId)
                .map(licenca -> licenca.pagamentoEmDia()
                        && licenca.getSaldoSms() != null
                        && licenca.getSaldoSms() > 0)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public SaldoResponse consultarSaldo(Long empresaId) {
        return licencaRepository.buscarPorEmpresaId(empresaId)
                .map(licenca -> new SaldoResponse(licenca.getSaldoSms(), licenca.getTipoPlano()))
                .orElseThrow(() -> new RegraNegocioException("Licença não encontrada."));
    }

    @Transactional(readOnly = true)
    public LicencaResponse detalhar(Long empresaId) {
        return licencaRepository.buscarPorEmpresaId(empresaId)
                .map(LicencaResponse::de)
                .orElseThrow(() -> new RegraNegocioException("Licença não encontrada."));
    }

    @Transactional
    public LicencaResponse recarregarManual(Long empresaId, int quantidade) {
        LicencaEntity licenca = licencaRepository.buscarPorEmpresaIdParaAtualizacao(empresaId)
                .orElseThrow(() -> new RegraNegocioException("Licença não encontrada para a empresa informada."));
        licenca.adicionarCreditosSms(quantidade);
        LOGGER.info("Recarga manual de {} crédito(s) aplicada à empresa {}.", quantidade, empresaId);
        return LicencaResponse.de(licenca);
    }

    @Transactional
    public LicencaResponse atualizar(Long empresaId, AtualizarLicencaRequest requisicao) {
        LicencaEntity licenca = licencaRepository.buscarPorEmpresaIdParaAtualizacao(empresaId)
                .orElseThrow(() -> new RegraNegocioException("Licença não encontrada para a empresa informada."));
        licenca.atualizarPlano(requisicao.getTipoPlano());
        licenca.definirStatusPagamento(requisicao.getStatusPagamento());
        licenca.definirDataRenovacao(requisicao.getDataRenovacao());
        return LicencaResponse.de(licenca);
    }

    public RecargaOnlineResponse iniciarRecargaOnline(Long empresaId, int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade de créditos deve ser positiva.");
        }
        return gatewayPagamento.iniciarRecarga(empresaId, quantidade);
    }
}

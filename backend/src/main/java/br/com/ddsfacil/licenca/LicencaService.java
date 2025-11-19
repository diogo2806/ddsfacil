package br.com.ddsfacil.licenca;

import br.com.ddsfacil.excecao.RegraNegocioException;
import br.com.ddsfacil.licenca.dto.SaldoResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LicencaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicencaService.class);
    private final LicencaRepository licencaRepository;

    public LicencaService(LicencaRepository licencaRepository) {
        this.licencaRepository = licencaRepository;
    }

    @Transactional
    public void debitarSms(Long empresaId, int quantidade) {
        LicencaEntity licenca = licencaRepository.buscarPorEmpresaIdParaAtualizacao(empresaId)
                .orElseThrow(() -> new RegraNegocioException("Licença não encontrada para a empresa informada."));
        licenca.consumirSaldoSms(quantidade);
        LOGGER.info("Saldo de SMS da empresa {} debitado em {} crédito(s).", empresaId, quantidade);
    }

    @Transactional(readOnly = true)
    public SaldoResponse consultarSaldo(Long empresaId) {
        return licencaRepository.buscarPorEmpresaId(empresaId)
                .map(licenca -> new SaldoResponse(licenca.getSaldoSms(), licenca.getTipoPlano()))
                .orElseThrow(() -> new RegraNegocioException("Licença não encontrada."));
    }
}

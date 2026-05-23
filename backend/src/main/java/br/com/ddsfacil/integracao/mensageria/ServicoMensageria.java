package br.com.ddsfacil.integracao.mensageria;

import br.com.ddsfacil.envio.domain.CanalMensagem;

public interface ServicoMensageria {

    void enviarMensagem(CanalMensagem canal, String numeroDestino, String mensagem, Long envioId);
}

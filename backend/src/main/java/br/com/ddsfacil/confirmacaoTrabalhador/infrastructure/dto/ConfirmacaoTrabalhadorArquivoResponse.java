package br.com.ddsfacil.confirmacaoTrabalhador.infrastructure.dto;

import org.springframework.http.MediaType;

public class ConfirmacaoTrabalhadorArquivoResponse {

    private final String nomeArquivo;
    private final MediaType tipoMidia;
    private final byte[] dados;

    public ConfirmacaoTrabalhadorArquivoResponse(String nomeArquivo, MediaType tipoMidia, byte[] dados) {
        this.nomeArquivo = nomeArquivo;
        this.tipoMidia = tipoMidia;
        this.dados = dados;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public MediaType getTipoMidia() {
        return tipoMidia;
    }

    public byte[] getDados() {
        return dados;
    }
}

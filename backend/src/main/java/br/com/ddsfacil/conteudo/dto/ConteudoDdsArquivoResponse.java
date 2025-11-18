package br.com.ddsfacil.conteudo.dto;

import org.springframework.http.MediaType;

public class ConteudoDdsArquivoResponse {

    private final String nomeArquivo;
    private final MediaType tipoMidia;
    private final byte[] dados;

    public ConteudoDdsArquivoResponse(String nomeArquivo, MediaType tipoMidia, byte[] dados) {
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

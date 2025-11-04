package br.com.ddsfacil.confirmacaoTrabalhador.dto;

public class ConfirmacaoTrabalhadorResponse {

    private final String titulo;
    private final String descricao;

    public ConfirmacaoTrabalhadorResponse(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }
}

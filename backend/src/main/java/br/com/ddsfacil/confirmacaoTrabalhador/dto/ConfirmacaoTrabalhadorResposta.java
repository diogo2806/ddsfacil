package br.com.ddsfacil.confirmacaoTrabalhador.dto;

public class ConfirmacaoTrabalhadorResposta {

    private final String titulo;
    private final String descricao;

    public ConfirmacaoTrabalhadorResposta(String titulo, String descricao) {
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

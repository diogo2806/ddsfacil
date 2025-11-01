package br.com.ddsfacil.conteudo.dto;

public class ConteudoDdsResposta {

    private final Long id;
    private final String titulo;
    private final String descricao;

    public ConteudoDdsResposta(Long id, String titulo, String descricao) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }
}

package br.com.ddsfacil.funcionario.dto;

public class FuncionarioResposta {

    private final Long id;
    private final String nome;
    private final String celular;
    private final String obra;

    public FuncionarioResposta(Long id, String nome, String celular, String obra) {
        this.id = id;
        this.nome = nome;
        this.celular = celular;
        this.obra = obra;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCelular() {
        return celular;
    }

    public String getObra() {
        return obra;
    }
}

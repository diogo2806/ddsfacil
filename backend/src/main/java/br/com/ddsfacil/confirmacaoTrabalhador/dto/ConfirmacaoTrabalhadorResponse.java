package br.com.ddsfacil.confirmacaoTrabalhador.dto;

public class ConfirmacaoTrabalhadorResponse {

    private final String titulo;
    private final String descricao;
    private final br.com.ddsfacil.conteudo.TipoConteudo tipoConteudo;
    private final String urlConteudo;
    private final String nomeArquivo;

    public ConfirmacaoTrabalhadorResponse(
            String titulo,
            String descricao,
            br.com.ddsfacil.conteudo.TipoConteudo tipoConteudo,
            String urlConteudo,
            String nomeArquivo
    ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipoConteudo = tipoConteudo;
        this.urlConteudo = urlConteudo;
        this.nomeArquivo = nomeArquivo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public br.com.ddsfacil.conteudo.TipoConteudo getTipoConteudo() {
        return tipoConteudo;
    }

    public String getUrlConteudo() {
        return urlConteudo;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }
}

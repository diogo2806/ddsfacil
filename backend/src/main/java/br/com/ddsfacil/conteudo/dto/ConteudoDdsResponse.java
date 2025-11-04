// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/dto/ConteudoDdsResposta.java
package br.com.ddsfacil.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para resposta de um conteúdo de DDS")
public class ConteudoDdsResposta {

    @Schema(description = "ID único do conteúdo", example = "1")
    private final Long id;

    @Schema(description = "Título principal do DDS", example = "Segurança em Altura")
    private final String titulo;

    @Schema(description = "Descrição detalhada do DDS", example = "Sempre use o cinto de segurança...")
    private final String descricao;

    @Schema(description = "Tipo do conteúdo", example = "TEXTO")
    private final String tipo;

    @Schema(description = "URL (se tipo LINK)", example = "https://site.com/video")
    private final String url;

    @Schema(description = "Nome do arquivo (se tipo ARQUIVO)", example = "manual.pdf")
    private final String arquivoNome;

    @Schema(description = "Caminho do arquivo (se tipo ARQUIVO)", example = "/uploads/12345-manual.pdf")
    private final String arquivoPath;

    public ConteudoDdsResposta(Long id, String titulo, String descricao, String tipo, String url, String arquivoNome, String arquivoPath) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.url = url;
        this.arquivoNome = arquivoNome;
        this.arquivoPath = arquivoPath;
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

    public String getTipo() {
        return tipo;
    }

    public String getUrl() {
        return url;
    }

    public String getArquivoNome() {
        return arquivoNome;
    }

    public String getArquivoPath() {
        return arquivoPath;
    }
}
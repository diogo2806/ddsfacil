// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/dto/ConteudoDdsRequisicao.java
package br.com.ddsfacil.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para criação de um novo conteúdo de DDS")
public class ConteudoDdsRequisicao {

    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 120, message = "O título deve ter no máximo 120 caracteres.")
    @Schema(description = "Título principal do DDS", example = "Segurança em Altura", requiredMode = Schema.RequiredMode.REQUIRED)
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória.")
    @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
    @Schema(description = "Descrição detalhada do DDS (para tipo TEXTO)", example = "Sempre use o cinto de segurança...")
    private String descricao;

    @Schema(description = "Tipo do conteúdo", example = "TEXTO", allowableValues = {"TEXTO", "LINK", "ARQUIVO"})
    private String tipo;

    @Schema(description = "URL (para tipo LINK)", example = "https://site.com/video")
    private String url;

    @Schema(description = "Nome do arquivo (para tipo ARQUIVO, gerenciado pelo endpoint /upload)", example = "manual.pdf")
    private String arquivoNome;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArquivoNome() {
        return arquivoNome;
    }

    public void setArquivoNome(String arquivoNome) {
        this.arquivoNome = arquivoNome;
    }
}
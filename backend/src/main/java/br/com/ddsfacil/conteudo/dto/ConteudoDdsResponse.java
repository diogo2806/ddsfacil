// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/dto/ConteudoDdsResponse.java
package br.com.ddsfacil.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "DTO para resposta de um conteúdo de DDS")
@Getter
public class ConteudoDdsResponse {

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

    @Schema(description = "Conteúdo binário do arquivo (se tipo ARQUIVO)")
    private final byte[] arquivoDados;

    public ConteudoDdsResponse(Long id, String titulo, String descricao, String tipo, String url, String arquivoNome, byte[] arquivoDados) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.url = url;
        this.arquivoNome = arquivoNome;
        this.arquivoDados = arquivoDados;
    }
}
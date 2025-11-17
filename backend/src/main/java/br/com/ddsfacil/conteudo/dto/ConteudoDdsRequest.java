// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/dto/ConteudoDdsRequest.java
package br.com.ddsfacil.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "DTO para criação de um novo conteúdo de DDS")
@Getter
@Setter
public class ConteudoDdsRequest {

    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 120, message = "O título deve ter no máximo 120 caracteres.")
    @Schema(description = "Título principal do DDS", example = "Segurança em Altura", requiredMode = Schema.RequiredMode.REQUIRED)
    private String titulo;

    // CORREÇÃO: Removido @NotBlank para permitir descrição vazia em Links/Arquivos
    @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
    @Schema(description = "Descrição detalhada do DDS (para tipo TEXTO)", example = "Sempre use o cinto de segurança...")
    private String descricao;

    @Schema(description = "Tipo do conteúdo", example = "TEXTO", allowableValues = {"TEXTO", "LINK", "ARQUIVO"})
    private String tipo;

    @Schema(description = "URL (para tipo LINK)", example = "https://site.com/video")
    private String url;

    @Schema(description = "Nome do arquivo (para tipo ARQUIVO, gerenciado pelo endpoint /upload)", example = "manual.pdf")
    private String arquivoNome;

    @Schema(description = "Arquivo recebido no upload (apenas para tipo ARQUIVO)", type = "string", format = "binary")
    private MultipartFile arquivo;
}

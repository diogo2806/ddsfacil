// Arquivo: backend/src/main/java/br/com/ddsfacil/envio/infrastructure/dto/EnvioDdsResponse.java
package br.com.ddsfacil.envio.infrastructure.dto;

import br.com.ddsfacil.envio.domain.StatusEnvioDds;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

@Schema(description = "DTO de resposta para um envio de DDS registrado")
@Getter
public class EnvioDdsResponse {

    @Schema(description = "ID único do envio", example = "1")
    private final Long id;

    @Schema(description = "ID do funcionário", example = "1")
    private final Long funcionarioId;

    @Schema(description = "Nome do funcionário", example = "Ana Souza")
    private final String nomeFuncionario;

    @Schema(description = "Obra do funcionário", example = "Obra Centro")
    private final String obra;

    @Schema(description = "ID do conteúdo enviado", example = "1")
    private final Long conteudoId;

    @Schema(description = "Título do conteúdo enviado", example = "Segurança em Altura")
    private final String tituloConteudo;

    @Schema(description = "Status do envio", example = "Confirmado")
    private final StatusEnvioDds status;

    @Schema(description = "Data do envio", example = "2025-11-04")
    private final LocalDate dataEnvio;

    @Schema(description = "Momento exato do envio")
    private final LocalDateTime momentoEnvio;

    @Schema(description = "Momento da confirmação (se houver)")
    private final LocalDateTime momentoConfirmacao;

    @Schema(description = "Descrição da falha de entrega, se houver")
    private final String mensagemErroEntrega;

    public EnvioDdsResponse(
            Long id,
            Long funcionarioId,
            String nomeFuncionario,
            String obra,
            Long conteudoId,
            String tituloConteudo,
            StatusEnvioDds status,
            LocalDate dataEnvio,
            LocalDateTime momentoEnvio,
            LocalDateTime momentoConfirmacao,
            String mensagemErroEntrega
    ) {
        this.id = id;
        this.funcionarioId = funcionarioId;
        this.nomeFuncionario = nomeFuncionario;
        this.obra = obra;
        this.conteudoId = conteudoId;
        this.tituloConteudo = tituloConteudo;
        this.status = status;
        this.dataEnvio = dataEnvio;
        this.momentoEnvio = momentoEnvio;
        this.momentoConfirmacao = momentoConfirmacao;
        this.mensagemErroEntrega = mensagemErroEntrega;
    }
}
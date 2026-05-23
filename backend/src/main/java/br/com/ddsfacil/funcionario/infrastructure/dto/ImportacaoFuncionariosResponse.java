package br.com.ddsfacil.funcionario.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;

@Schema(description = "Resumo do resultado de uma importação de funcionários via arquivo")
@Getter
public class ImportacaoFuncionariosResponse {

    private final int totalLinhas;
    private final int importados;
    private final List<ErroLinha> erros;

    public ImportacaoFuncionariosResponse(int totalLinhas, int importados, List<ErroLinha> erros) {
        this.totalLinhas = totalLinhas;
        this.importados = importados;
        this.erros = erros;
    }

    @Getter
    public static class ErroLinha {
        private final int linha;
        private final String motivo;

        public ErroLinha(int linha, String motivo) {
            this.linha = linha;
            this.motivo = motivo;
        }
    }
}

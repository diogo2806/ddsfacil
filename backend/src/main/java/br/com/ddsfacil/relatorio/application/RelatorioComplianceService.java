package br.com.ddsfacil.relatorio.application;

import br.com.ddsfacil.conteudo.domain.ConteudoDdsEntity;
import br.com.ddsfacil.conteudo.infrastructure.ConteudoDdsRepository;
import br.com.ddsfacil.envio.domain.EnvioDdsEntity;
import br.com.ddsfacil.envio.infrastructure.EnvioDdsRepository;
import br.com.ddsfacil.envio.domain.StatusEnvioDds;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.excecao.RegraNegocioException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.jsoup.nodes.Document.OutputSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatorioComplianceService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final EnvioDdsRepository envioRepositorio;
    private final ConteudoDdsRepository conteudoRepositorio;

    public RelatorioComplianceService(EnvioDdsRepository envioRepositorio, ConteudoDdsRepository conteudoRepositorio) {
        this.envioRepositorio = envioRepositorio;
        this.conteudoRepositorio = conteudoRepositorio;
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdfEvidencia(Long conteudoId, LocalDate dataEnvio, Long localTrabalhoId) {
        if (conteudoId == null) {
            throw new RegraNegocioException("O identificador do conteúdo é obrigatório para gerar o relatório.");
        }
        if (dataEnvio == null) {
            throw new RegraNegocioException("A data de envio é obrigatória para gerar o relatório de compliance.");
        }

        ConteudoDdsEntity conteudo = conteudoRepositorio
                .findById(conteudoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo informado não foi encontrado."));

        List<EnvioDdsEntity> enviosConfirmados = envioRepositorio.buscarConfirmadosPorDataConteudoStatusELocal(
                dataEnvio,
                conteudoId,
                StatusEnvioDds.CONFIRMADO,
                localTrabalhoId
        );

        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(documento, saida);

            documento.open();
            adicionarCabecalho(documento, dataEnvio);
            adicionarSecaoConteudo(documento, conteudo);
            adicionarTabelaPresenca(documento, enviosConfirmados);
            adicionarRodapeLegal(documento);
            documento.close();

            return saida.toByteArray();
        } catch (DocumentException e) {
            throw new RegraNegocioException("Erro ao montar o documento PDF de evidência de compliance.");
        } catch (Exception e) {
            throw new RegraNegocioException("Erro inesperado ao gerar o relatório de compliance.");
        }
    }

    private void adicionarCabecalho(Document documento, LocalDate dataEnvio) throws DocumentException {
        Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fontePadrao = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph nomeEmpresa = new Paragraph("DDS Fácil - Evidência Oficial", fontePadrao);
        nomeEmpresa.setAlignment(Element.ALIGN_CENTER);
        nomeEmpresa.setSpacingAfter(6f);
        documento.add(nomeEmpresa);

        Paragraph titulo = new Paragraph(
                "Relatório de Realização de Diálogo Diário de Segurança (DDS)",
                fonteTitulo
        );
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(8f);
        documento.add(titulo);

        Paragraph dataReferencia = new Paragraph(
                "Data de Referência: " + dataEnvio.format(FORMATO_DATA),
                fontePadrao
        );
        dataReferencia.setAlignment(Element.ALIGN_CENTER);
        dataReferencia.setSpacingAfter(18f);
        documento.add(dataReferencia);
    }

    private void adicionarSecaoConteudo(Document documento, ConteudoDdsEntity conteudo) throws DocumentException {
        Font fonteSecao = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
        Font fontePadrao = FontFactory.getFont(FontFactory.HELVETICA, 11);

        documento.add(new Paragraph("1. Conteúdo Ministrado", fonteSecao));
        documento.add(espacamentoPequeno());

        Paragraph tituloConteudo = new Paragraph(
                "Título: " + limparTexto(conteudo.getTitulo()),
                fontePadrao
        );
        tituloConteudo.setSpacingAfter(6f);
        documento.add(tituloConteudo);

        Paragraph descricaoConteudo = new Paragraph(
                "Descrição:\n" + limparTexto(conteudo.getDescricao()),
                fontePadrao
        );
        descricaoConteudo.setSpacingAfter(16f);
        documento.add(descricaoConteudo);
    }

    private void adicionarTabelaPresenca(Document documento, List<EnvioDdsEntity> envios) throws DocumentException {
        Font fonteSecao = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
        documento.add(new Paragraph("2. Lista de Presença (apenas confirmações)", fonteSecao));
        documento.add(espacamentoPequeno());

        PdfPTable tabela = new PdfPTable(new float[]{3f, 2f, 1.6f, 1.6f, 2.1f});
        tabela.setWidthPercentage(100f);
        adicionarCabecalhosTabela(tabela);

        if (envios.isEmpty()) {
            PdfPCell celulaVazia = new PdfPCell(new Phrase("Nenhum colaborador confirmou o recebimento na data selecionada."));
            celulaVazia.setColspan(5);
            celulaVazia.setHorizontalAlignment(Element.ALIGN_CENTER);
            celulaVazia.setPadding(8f);
            tabela.addCell(celulaVazia);
            documento.add(tabela);
            return;
        }

        for (EnvioDdsEntity envio : envios) {
            tabela.addCell(criarCelulaTexto(limparTexto(envio.getFuncionarioEntity().getNome())));
            tabela.addCell(criarCelulaTexto(limparTexto(envio.getFuncionarioEntity().getLocalTrabalho().getNome())));

            String horaEnvio = Optional.ofNullable(envio.getMomentoEnvio())
                    .map(momento -> momento.format(FORMATO_HORA))
                    .orElse("-");
            tabela.addCell(criarCelulaTexto(horaEnvio));

            String horaConfirmacao = Optional.ofNullable(envio.getMomentoConfirmacao())
                    .map(momento -> momento.format(FORMATO_HORA))
                    .orElse("-");
            tabela.addCell(criarCelulaTexto(horaConfirmacao));

            String tokenVisual = gerarTokenVisual(envio.getTokenAcesso());
            tabela.addCell(criarCelulaTexto(tokenVisual));
        }

        documento.add(tabela);
    }

    private void adicionarCabecalhosTabela(PdfPTable tabela) {
        Font fonteCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        String[] titulos = new String[]{
                "Nome Completo",
                "Setor/Obra",
                "Horário de Envio",
                "Horário de Confirmação",
                "Autenticação Digital"
        };

        for (String titulo : titulos) {
            PdfPCell cabecalho = new PdfPCell(new Phrase(titulo, fonteCabecalho));
            cabecalho.setHorizontalAlignment(Element.ALIGN_CENTER);
            cabecalho.setBackgroundColor(new java.awt.Color(230, 230, 230));
            cabecalho.setPaddingTop(6f);
            cabecalho.setPaddingBottom(6f);
            tabela.addCell(cabecalho);
        }
    }

    private PdfPCell criarCelulaTexto(String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celula.setPadding(6f);
        celula.setBorder(Rectangle.BOTTOM);
        return celula;
    }

    private void adicionarRodapeLegal(Document documento) throws DocumentException {
        Font fonteRodape = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);
        Paragraph rodape = new Paragraph(
                "Documento gerado eletronicamente pelo sistema DDS Fácil em "
                        + LocalDateTime.now().format(FORMATO_DATA_HORA)
                        + ". A autenticidade pode ser verificada via sistema.",
                fonteRodape
        );
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setSpacingBefore(14f);
        documento.add(rodape);
    }

    private Paragraph espacamentoPequeno() {
        Paragraph espacamento = new Paragraph(" ");
        espacamento.setSpacingAfter(6f);
        return espacamento;
    }

    private String gerarTokenVisual(String tokenAcesso) {
        String tokenLimpo = limparTexto(Optional.ofNullable(tokenAcesso).orElse(""));
        if (tokenLimpo.isBlank()) {
            return "N/A";
        }
        String tokenSemSeparadores = tokenLimpo.replaceAll("[^A-Za-z0-9]", "");
        String trechoToken = tokenSemSeparadores.length() >= 8
                ? tokenSemSeparadores.substring(0, 8).toUpperCase()
                : tokenSemSeparadores.toUpperCase();
        return "#" + trechoToken;
    }

    private String limparTexto(String texto) {
        OutputSettings configuracaoSaida = new OutputSettings();
        configuracaoSaida.prettyPrint(false);
        return Jsoup.clean(Optional.ofNullable(texto).orElse(""), "", Safelist.none(), configuracaoSaida);
    }
}

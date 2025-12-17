// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/api/ConteudoDdsController.java
package br.com.ddsfacil.conteudo.api;

import br.com.ddsfacil.conteudo.application.ConteudoDdsService;
import br.com.ddsfacil.conteudo.infrastructure.dto.ConteudoDdsArquivoResponse;
import br.com.ddsfacil.conteudo.infrastructure.dto.ConteudoDdsRequest;
import br.com.ddsfacil.conteudo.infrastructure.dto.ConteudoDdsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;

@RestController
@RequestMapping("/api/conteudos")
@Tag(name = "Conteúdos DDS", description = "Gerenciamento da biblioteca de conteúdos de DDS")
public class ConteudoDdsController {

    private final ConteudoDdsService conteudoServico;

    public ConteudoDdsController(ConteudoDdsService conteudoServico) {
        this.conteudoServico = conteudoServico;
    }

    @GetMapping
    @Operation(summary = "Lista todos os conteúdos de DDS cadastrados")
    public List<ConteudoDdsResponse> listar() {
        return conteudoServico.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo conteúdo de DDS (via JSON, para Texto ou Link)")
    public ConteudoDdsResponse criar(@Valid @RequestBody ConteudoDdsRequest requisicao) {
        return conteudoServico.criar(requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um conteúdo de DDS pelo ID")
    public void excluir(@PathVariable Long id) {
        conteudoServico.remover(id);
    }

    @GetMapping(path = "/{id}/arquivo")
    @Operation(summary = "Recupera o arquivo binário de um conteúdo do tipo ARQUIVO")
    public ResponseEntity<ByteArrayResource> baixarArquivo(@PathVariable Long id) {
        ConteudoDdsArquivoResponse arquivo = conteudoServico.buscarArquivo(id);
        ByteArrayResource recurso = new ByteArrayResource(arquivo.getDados());

        return ResponseEntity.ok()
                // Força o navegador a baixar novamente
                .cacheControl(CacheControl.noCache().mustRevalidate())
                // Define o tamanho para o navegador saber quando o download termina
                .contentLength(arquivo.getDados().length)
                .contentType(arquivo.getTipoMidia())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + arquivo.getNomeArquivo() + "\"")
                .body(recurso);
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo conteúdo de DDS (via Multipart-Form, para upload de Arquivo)")
    public ConteudoDdsResponse uploadArquivo(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam("file") MultipartFile file
    ) {
        String nomeOriginal = StringUtils.cleanPath(file.getOriginalFilename());

        ConteudoDdsRequest req = new ConteudoDdsRequest();
        req.setTitulo(titulo);
        req.setDescricao(descricao == null ? "" : descricao);
        req.setTipo(tipo == null ? "ARQUIVO" : tipo); // O serviço fará a conversão
        req.setArquivoNome(nomeOriginal);
        req.setArquivo(file);
        return conteudoServico.criar(req);
    }
}
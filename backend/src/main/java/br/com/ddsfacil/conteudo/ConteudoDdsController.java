// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/ConteudoDdsController.java
package br.com.ddsfacil.conteudo;

import br.com.ddsfacil.conteudo.dto.ConteudoDdsRequest;
import br.com.ddsfacil.conteudo.dto.ConteudoDdsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path; // 1. Importar Path
import java.nio.file.Paths; // 2. Importar Paths
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo conteúdo de DDS (via Multipart-Form, para upload de Arquivo)")
    public ConteudoDdsResponse uploadArquivo(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        // --- CORREÇÃO INICIA AQUI ---

        // 3. Definir o diretório de uploads usando Path
        String uploadsDir = "uploads";
        Path uploadsPath = Paths.get(uploadsDir);
        File uploadsFolder = uploadsPath.toFile();

        if (!uploadsFolder.exists()) {
            uploadsFolder.mkdirs();
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String targetName = System.currentTimeMillis() + "-" + original;

        // 4. Criar o caminho de destino absoluto
        Path destino = uploadsPath.resolve(targetName).toAbsolutePath();

        // 5. Usar o método transferTo(Path)
        file.transferTo(destino);

        // --- CORREÇÃO TERMINA AQUI ---

        // construir requisicao manualmente e delegar a serviço
        ConteudoDdsRequest req = new ConteudoDdsRequest();
        req.setTitulo(titulo);
        req.setDescricao(descricao == null ? "" : descricao);
        req.setTipo(tipo == null ? "ARQUIVO" : tipo); // O serviço fará a conversão
        req.setArquivoNome(original);
        req.setUrl("/uploads/" + targetName); // A URL salva no banco continua relativa (correto)
        return conteudoServico.criar(req);
    }
}
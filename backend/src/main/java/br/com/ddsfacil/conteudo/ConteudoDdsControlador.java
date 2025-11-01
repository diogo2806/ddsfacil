package br.com.ddsfacil.conteudo;

import br.com.ddsfacil.conteudo.dto.ConteudoDdsRequisicao;
import br.com.ddsfacil.conteudo.dto.ConteudoDdsResposta;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conteudos")
@CrossOrigin(origins = "http://localhost:5173")
public class ConteudoDdsControlador {

    private final ConteudoDdsServico conteudoServico;

    public ConteudoDdsControlador(ConteudoDdsServico conteudoServico) {
        this.conteudoServico = conteudoServico;
    }

    @GetMapping
    public List<ConteudoDdsResposta> listar() {
        return conteudoServico.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConteudoDdsResposta criar(@Valid @RequestBody ConteudoDdsRequisicao requisicao) {
        return conteudoServico.criar(requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        conteudoServico.remover(id);
    }
}

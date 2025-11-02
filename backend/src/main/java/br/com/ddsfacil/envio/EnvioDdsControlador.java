package br.com.ddsfacil.envio;

import br.com.ddsfacil.envio.dto.EnvioDdsRequisicao;
import br.com.ddsfacil.envio.dto.EnvioDdsResposta;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "http://localhost:5173")
public class EnvioDdsControlador {

    private final EnvioDdsServico envioServico;

    public EnvioDdsControlador(EnvioDdsServico envioServico) {
        this.envioServico = envioServico;
    }

    @GetMapping
    public List<EnvioDdsResposta> listarPorData(
        @RequestParam(name = "data", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate data
    ) {
        return envioServico.listarPorData(data);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<EnvioDdsResposta> enviar(@Valid @RequestBody EnvioDdsRequisicao requisicao) {
        return envioServico.enviar(requisicao);
    }

    @PostMapping("/{id}/confirmacoes")
    public EnvioDdsResposta confirmar(@PathVariable Long id) {
        return envioServico.confirmar(id);
    }
}

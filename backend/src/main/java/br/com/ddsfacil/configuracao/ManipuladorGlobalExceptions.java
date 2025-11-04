package br.com.ddsfacil.configuracao;

import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import java.util.HashMap;
import java.util.Map;

import br.com.ddsfacil.excecao.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManipuladorGlobalExceptions {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarErrosValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> mensagens = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            mensagens.put(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(mensagens);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, String>> tratarRegraNegocio(RegraNegocioException ex) {
        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
    }
}

// Arquivo: backend/src/main/java/br/com/ddsfacil/excecao/RegraNegocioException.java
package br.com.ddsfacil.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção para violações de regras de negócio que devem retornar HTTP 409 (Conflict).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}

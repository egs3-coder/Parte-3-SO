package br.cesar.rotavital.portal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.cesar.rotavital.compartilhado.aplicacao.ConflitoException;
import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;

/**
 * Converte os erros de todos os contextos em Problem Details (RFC 9457), como na Parte 1.
 * Regra de domínio quebrada = 422, com a mensagem da linguagem ubíqua no "detail".
 */
@RestControllerAdvice
public class TratamentoDeErros extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NaoEncontradoException.class)
    ProblemDetail naoEncontrado(NaoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    ProblemDetail conflito(ConflitoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    ProblemDetail regraDeNegocio(RegraDeNegocioException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        if (!e.motivos().isEmpty()) {
            problema.setProperty("motivos", e.motivos());
        }
        return problema;
    }

    /** Duas pessoas mexeram na mesma bolsa ou requisição ao mesmo tempo. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail concorrencia(ObjectOptimisticLockingFailureException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "O registro foi alterado por outra operação ao mesmo tempo; tente de novo");
    }

    /** Objeto de valor inválido (CPF, tipo sanguíneo, código...). */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail argumentoInvalido(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}

package br.cesar.rotavital.compartilhado.aplicacao;

/** O recurso pedido não existe (vira HTTP 404 na apresentação). */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

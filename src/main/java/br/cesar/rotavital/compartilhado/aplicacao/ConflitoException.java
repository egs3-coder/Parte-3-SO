package br.cesar.rotavital.compartilhado.aplicacao;

/** O pedido conflita com algo que já existe (CPF repetido, código de bolsa repetido): HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}

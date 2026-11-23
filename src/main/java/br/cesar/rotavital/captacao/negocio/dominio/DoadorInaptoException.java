package br.cesar.rotavital.captacao.negocio.dominio;

import java.util.List;

import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;

/** A triagem não aprovou o doador; carrega todos os motivos para o atendente explicar de uma vez. */
public class DoadorInaptoException extends RegraDeNegocioException {

    public DoadorInaptoException(List<String> motivos) {
        super("Doador inapto para doação", motivos);
    }
}

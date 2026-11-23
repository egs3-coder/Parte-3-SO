package br.cesar.rotavital.requisicao.negocio.dominio;

import java.util.Objects;

import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;

/** O que se pede num item ao registrar a requisição. */
public record NovoItem(Hemocomponente hemocomponente, TipoSanguineo tipoReceptor, Quantidade quantidade) {

    public NovoItem {
        Objects.requireNonNull(hemocomponente, "Item precisa de hemocomponente");
        Objects.requireNonNull(tipoReceptor, "Item precisa do tipo do receptor");
        Objects.requireNonNull(quantidade, "Item precisa de quantidade");
    }
}

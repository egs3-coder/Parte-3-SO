package br.cesar.rotavital.requisicao.negocio.dominio;

import java.util.Objects;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.compartilhado.dominio.Validade;

/**
 * Objeto de valor: o que a Requisição sabe de uma bolsa que o Estoque reservou para ela (diagrama B.1 da Parte 2).
 * É uma cópia, na linguagem da Requisição; a bolsa "de verdade" continua sendo do Estoque.
 */
public record BolsaReservada(CodigoBolsa codigo, Hemocomponente hemocomponente, TipoSanguineo tipoSanguineo,
                             Validade validade) {

    public BolsaReservada {
        Objects.requireNonNull(codigo, "Bolsa precisa de código");
        Objects.requireNonNull(hemocomponente, "Bolsa precisa de hemocomponente");
        Objects.requireNonNull(tipoSanguineo, "Bolsa precisa de tipo sanguíneo");
        Objects.requireNonNull(validade, "Bolsa precisa de validade");
    }
}

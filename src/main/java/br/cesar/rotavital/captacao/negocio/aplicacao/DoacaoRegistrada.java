package br.cesar.rotavital.captacao.negocio.aplicacao;

import java.time.LocalDate;

import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;

/**
 * Evento de integração publicado pela Captação quando uma doação é registrada.
 * O Estoque consome e dá entrada da bolsa ({@code EntradaDeBolsaPorDoacao}); a Captação não conhece o Estoque.
 *
 * @param codigoBolsa número da doação, que vira o código da bolsa (linguagem publicada)
 */
public record DoacaoRegistrada(String codigoBolsa, TipoSanguineo tipoSanguineo, LocalDate dataColeta) {
}

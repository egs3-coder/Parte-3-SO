package br.cesar.rotavital.compartilhado.dominio;

import java.util.regex.Pattern;

/**
 * Objeto de valor: identificador da bolsa, a linguagem publicada entre Captação, Estoque, Requisição e Expedição
 * (na Parte 2, o código ISBT 128). Os contextos se referem à bolsa só por ele, nunca pela linha da tabela do Estoque.
 */
public record CodigoBolsa(String valor) {

    private static final Pattern FORMATO = Pattern.compile("[A-Z0-9][A-Z0-9-]{1,29}");

    public CodigoBolsa {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Código da bolsa é obrigatório");
        }
        valor = valor.trim().toUpperCase();
        if (!FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Código da bolsa inválido: " + valor);
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}

package br.cesar.rotavital.rede.negocio.dominio;

import java.util.Set;

/** Objeto de valor: cidade e UF da unidade. Imutável e validado no construtor. */
public record Endereco(String cidade, String uf) {

    private static final Set<String> UFS = Set.of("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT",
            "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    public Endereco {
        if (cidade == null || cidade.isBlank()) {
            throw new IllegalArgumentException("Cidade é obrigatória");
        }
        if (uf == null || !UFS.contains(uf.trim().toUpperCase())) {
            throw new IllegalArgumentException("UF inválida: " + uf);
        }
        cidade = cidade.trim();
        uf = uf.trim().toUpperCase();
    }
}

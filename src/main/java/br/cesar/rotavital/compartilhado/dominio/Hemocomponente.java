package br.cesar.rotavital.compartilhado.dominio;

import java.time.LocalDate;

/**
 * Objeto de valor: o que a bolsa contém. Nesta fase, concentrado de hemácias e plasma fresco congelado.
 */
public enum Hemocomponente {

    CONCENTRADO_HEMACIAS("Concentrado de hemácias", 35),
    PLASMA_FRESCO_CONGELADO("Plasma fresco congelado", 365);

    private final String descricao;
    private final int diasDeValidade;

    Hemocomponente(String descricao, int diasDeValidade) {
        this.descricao = descricao;
        this.diasDeValidade = diasDeValidade;
    }

    /** Hemácias em CPDA-1: 35 dias. Plasma fresco congelado: 12 meses. Vale até o fim do último dia. */
    public Validade validadeParaColetaEm(LocalDate dataColeta) {
        return Validade.ateOFimDoDia(dataColeta.plusDays(diasDeValidade));
    }

    public int diasDeValidade() {
        return diasDeValidade;
    }

    public String descricao() {
        return descricao;
    }

    public static Hemocomponente deNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Hemocomponente é obrigatório");
        }
        try {
            return valueOf(nome.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Hemocomponente inválido: " + nome);
        }
    }
}

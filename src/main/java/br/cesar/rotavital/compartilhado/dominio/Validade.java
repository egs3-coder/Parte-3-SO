package br.cesar.rotavital.compartilhado.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Objeto de valor: data-limite de uso de uma bolsa. Imutável e validado no construtor.
 * A bolsa é válida até o instante {@link #limite()}, inclusive.
 */
public record Validade(LocalDateTime limite) implements Comparable<Validade> {

    /** Último minuto do dia: "vale até o fim do dia da validade". */
    private static final LocalTime FIM_DO_DIA = LocalTime.of(23, 59);

    public Validade {
        Objects.requireNonNull(limite, "A validade precisa de uma data-limite");
    }

    public static Validade ateOFimDoDia(LocalDate dia) {
        Objects.requireNonNull(dia, "A validade precisa de uma data");
        return new Validade(dia.atTime(FIM_DO_DIA));
    }

    /** A bolsa ainda pode ser usada no momento informado? */
    public boolean estaValidaEm(LocalDateTime momento) {
        return !momento.isAfter(limite);
    }

    public boolean vencidaEm(LocalDateTime momento) {
        return !estaValidaEm(momento);
    }

    @Override
    public int compareTo(Validade outra) {
        return limite.compareTo(outra.limite);
    }
}

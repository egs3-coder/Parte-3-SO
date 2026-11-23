package br.cesar.rotavital.compartilhado.dominio;

import java.util.EnumSet;
import java.util.Set;

/**
 * Objeto de valor: tipo sanguíneo = grupo ABO + fator Rh (ex.: O−).
 *
 * <p>Faz parte do núcleo compartilhado (shared kernel): Captação, Estoque e Requisição usam o mesmo conceito.
 * Ele só sabe quais antígenos cada tipo tem; a regra de compatibilidade depende do hemocomponente e fica em
 * {@link Hemocomponente#compativel(TipoSanguineo, TipoSanguineo)} (invariante I1 da Requisição).
 */
public enum TipoSanguineo {

    O_NEGATIVO("O-"),
    O_POSITIVO("O+", Antigeno.D),
    A_NEGATIVO("A-", Antigeno.A),
    A_POSITIVO("A+", Antigeno.A, Antigeno.D),
    B_NEGATIVO("B-", Antigeno.B),
    B_POSITIVO("B+", Antigeno.B, Antigeno.D),
    AB_NEGATIVO("AB-", Antigeno.A, Antigeno.B),
    AB_POSITIVO("AB+", Antigeno.A, Antigeno.B, Antigeno.D);

    /** Antígenos da superfície da hemácia: A e B (sistema ABO) e D (fator Rh). */
    public enum Antigeno { A, B, D }

    private final String simbolo;
    private final Set<Antigeno> antigenos;

    TipoSanguineo(String simbolo, Antigeno... antigenos) {
        this.simbolo = simbolo;
        this.antigenos = antigenos.length == 0 ? EnumSet.noneOf(Antigeno.class) : EnumSet.of(antigenos[0], antigenos);
    }

    /** Cópia defensiva: ninguém de fora altera os antígenos de um tipo. */
    public Set<Antigeno> antigenos() {
        return antigenos.isEmpty() ? EnumSet.noneOf(Antigeno.class) : EnumSet.copyOf(antigenos);
    }

    /** Só os antígenos do sistema ABO (sem o D), usados na regra do plasma. */
    public Set<Antigeno> antigenosAbo() {
        Set<Antigeno> abo = antigenos();
        abo.remove(Antigeno.D);
        return abo;
    }

    public boolean rhPositivo() {
        return antigenos.contains(Antigeno.D);
    }

    public String simbolo() {
        return simbolo;
    }

    /** Aceita "O-", "o-", "AB+" e também o sinal de menos tipográfico ("O−"). */
    public static TipoSanguineo deSimbolo(String simbolo) {
        if (simbolo == null) {
            throw new IllegalArgumentException("Tipo sanguíneo é obrigatório");
        }
        String normalizado = simbolo.trim().replace('−', '-').toUpperCase();
        for (TipoSanguineo tipo : values()) {
            if (tipo.simbolo.equals(normalizado)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo sanguíneo inválido: " + simbolo);
    }

    @Override
    public String toString() {
        return simbolo;
    }
}

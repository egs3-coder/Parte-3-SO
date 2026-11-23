package br.cesar.rotavital.requisicao.negocio.dominio;

/** Objeto de valor: quantas bolsas o item pede (de 1 a 20). */
public record Quantidade(int valor) {

    public static final int MAXIMO = 20;

    public Quantidade {
        if (valor < 1 || valor > MAXIMO) {
            throw new IllegalArgumentException("Quantidade deve ser de 1 a " + MAXIMO + " bolsas");
        }
    }
}

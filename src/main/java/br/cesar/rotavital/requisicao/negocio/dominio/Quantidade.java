package br.cesar.rotavital.requisicao.negocio.dominio;

/** Objeto de valor: quantas bolsas o item pede (de 1 a 20). */
public record Quantidade(int valor) {

    public static final int MAXIMO = 20;

    public Quantidade {
        // TODO verde: de 1 a 20
    }
}

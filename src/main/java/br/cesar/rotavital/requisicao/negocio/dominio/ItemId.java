package br.cesar.rotavital.requisicao.negocio.dominio;

/** Objeto de valor: identidade do item dentro da requisição (ITEM-1, ITEM-2...). */
public record ItemId(String valor) {

    public ItemId {
        if (valor == null || !valor.matches("ITEM-[1-9]\\d*")) {
            throw new IllegalArgumentException("Id de item inválido: " + valor);
        }
    }

    public static ItemId daPosicao(int posicao) {
        return new ItemId("ITEM-" + posicao);
    }

    @Override
    public String toString() {
        return valor;
    }
}

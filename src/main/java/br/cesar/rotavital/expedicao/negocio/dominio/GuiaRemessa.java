package br.cesar.rotavital.expedicao.negocio.dominio;

/** Objeto de valor: número da guia de remessa (documento com as bolsas, origem, destino e responsável). */
public record GuiaRemessa(String numero) {

    public GuiaRemessa {
        if (numero == null || !numero.matches("GR-[A-Z0-9-]{3,40}")) {
            throw new IllegalArgumentException("Número de guia inválido: " + numero);
        }
    }

    /** Uma remessa por requisição: a guia leva o id da requisição. */
    public static GuiaRemessa daRequisicao(String requisicaoId) {
        return new GuiaRemessa("GR-" + requisicaoId);
    }
}

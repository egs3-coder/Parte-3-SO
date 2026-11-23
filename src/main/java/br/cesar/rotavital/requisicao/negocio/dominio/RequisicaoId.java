package br.cesar.rotavital.requisicao.negocio.dominio;

/** Objeto de valor: identidade da requisição (REQ-001). */
public record RequisicaoId(String valor) {

    public RequisicaoId {
        // TODO verde: formato REQ-001
    }

    public static RequisicaoId doNumero(long numero) {
        return new RequisicaoId(String.format("REQ-%03d", numero));
    }

    @Override
    public String toString() {
        return valor;
    }
}

package br.cesar.rotavital.requisicao.negocio.dominio;

/** Objeto de valor: identidade da requisição (REQ-001). */
public record RequisicaoId(String valor) {

    public RequisicaoId {
        if (valor == null || !valor.matches("REQ-\\d{3,}")) {
            throw new IllegalArgumentException("Id de requisição inválido: " + valor);
        }
    }

    public static RequisicaoId doNumero(long numero) {
        return new RequisicaoId(String.format("REQ-%03d", numero));
    }

    @Override
    public String toString() {
        return valor;
    }
}

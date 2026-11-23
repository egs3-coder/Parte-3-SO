package br.cesar.rotavital.requisicao.negocio.dominio;

/** Objeto de valor: justificativa clínica do pedido (obrigatória na emergência, invariante I4). */
public record JustificativaClinica(String texto) {

    public static final int TAMANHO_MAXIMO = 500;

    public JustificativaClinica {
        texto = texto == null ? "" : texto.trim();
        if (texto.length() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("Justificativa clínica deve ter até " + TAMANHO_MAXIMO + " caracteres");
        }
    }

    public static JustificativaClinica nenhuma() {
        return new JustificativaClinica("");
    }

    public boolean informada() {
        return !texto.isEmpty();
    }
}

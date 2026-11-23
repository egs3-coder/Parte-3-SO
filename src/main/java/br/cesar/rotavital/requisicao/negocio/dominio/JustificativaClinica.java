package br.cesar.rotavital.requisicao.negocio.dominio;

/** Objeto de valor: justificativa clínica do pedido (obrigatória na emergência, invariante I4). */
public record JustificativaClinica(String texto) {

    public static final int TAMANHO_MAXIMO = 500;

    public JustificativaClinica {
        // TODO verde: aparar e limitar a 500 caracteres
    }

    public static JustificativaClinica nenhuma() {
        return new JustificativaClinica("");
    }

    public boolean informada() {
        return texto != null;
    }
}

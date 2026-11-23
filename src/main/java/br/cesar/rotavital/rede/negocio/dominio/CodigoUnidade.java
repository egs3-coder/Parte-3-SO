package br.cesar.rotavital.rede.negocio.dominio;

/** Objeto de valor: código da unidade solicitante (ex.: HSA). */
public record CodigoUnidade(String valor) {

    public CodigoUnidade {
        if (valor == null || !valor.trim().toUpperCase().matches("[A-Z0-9][A-Z0-9-]{1,19}")) {
            throw new IllegalArgumentException("Código de unidade inválido: " + valor);
        }
        valor = valor.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return valor;
    }
}

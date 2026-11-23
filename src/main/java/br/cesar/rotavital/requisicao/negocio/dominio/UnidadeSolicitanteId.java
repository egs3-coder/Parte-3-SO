package br.cesar.rotavital.requisicao.negocio.dominio;

/** Objeto de valor: a unidade que pede, referida só pelo código publicado pela Rede. */
public record UnidadeSolicitanteId(String codigo) {

    public UnidadeSolicitanteId {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Unidade solicitante é obrigatória");
        }
        codigo = codigo.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return codigo;
    }
}

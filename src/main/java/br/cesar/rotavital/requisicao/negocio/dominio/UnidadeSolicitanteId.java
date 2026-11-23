package br.cesar.rotavital.requisicao.negocio.dominio;

/** Objeto de valor: a unidade que pede, referida só pelo código publicado pela Rede. */
public record UnidadeSolicitanteId(String codigo) {

    public UnidadeSolicitanteId {
        // TODO verde: obrigatório
    }

    @Override
    public String toString() {
        return codigo;
    }
}

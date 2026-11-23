package br.cesar.rotavital.requisicao.negocio.dominio;

/** Ciclo de vida da requisição (invariante I5). */
public enum StatusRequisicao {
    REGISTRADA,
    EM_ATENDIMENTO,
    ATENDIDA,
    EM_TRANSPORTE,
    ENTREGUE,
    CANCELADA
}

package br.cesar.rotavital.requisicao.negocio.dominio;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;

/**
 * Porta para o contexto Estoque (cliente-fornecedor). A Requisição não lê a tabela do Estoque: pede a reserva
 * e recebe, na sua própria linguagem, os dados da bolsa reservada. O adaptador fica em requisicao.dados.
 */
public interface EstoqueGateway {

    /** Comando ReservarBolsas da saga. Recusa se a bolsa não existe, já está reservada ou venceu. */
    BolsaReservada reservar(CodigoBolsa codigo, RequisicaoId requisicao);

    /** Comando LiberarReserva (compensação). */
    void liberar(CodigoBolsa codigo, RequisicaoId requisicao);
}

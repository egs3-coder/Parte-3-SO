package br.cesar.rotavital.requisicao.negocio.dominio;

import java.util.List;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;

/** Porta para a Expedição: comando MontarRemessa da saga; devolve o número da guia. */
public interface ExpedicaoGateway {

    String montarRemessa(RequisicaoId requisicao, UnidadeSolicitanteId destino, List<CodigoBolsa> bolsas);
}

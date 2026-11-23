package br.cesar.rotavital.requisicao.negocio.dominio;

/** Porta para o serviço aberto da Rede (OHS): a unidade existe e está ativa? */
public interface RedeGateway {

    boolean unidadeAtiva(UnidadeSolicitanteId unidade);
}

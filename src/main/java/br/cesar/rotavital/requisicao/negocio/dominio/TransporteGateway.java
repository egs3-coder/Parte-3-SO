package br.cesar.rotavital.requisicao.negocio.dominio;

/** Porta para o Transporte: comando AbrirViagem da saga (ponto sem volta); devolve o id da viagem. */
public interface TransporteGateway {

    String abrirViagem(RequisicaoId requisicao, String numeroGuia);
}

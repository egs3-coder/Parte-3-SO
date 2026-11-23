package br.cesar.rotavital.requisicao.dados;

import org.springframework.stereotype.Component;

import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.TransporteGateway;
import br.cesar.rotavital.transporte.negocio.aplicacao.AbrirViagem;

/** Adaptador da porta {@link TransporteGateway}: envia o comando AbrirViagem e devolve o id da viagem. */
@Component
class TransporteClient implements TransporteGateway {

    private final AbrirViagem abrirViagem;

    TransporteClient(AbrirViagem abrirViagem) {
        this.abrirViagem = abrirViagem;
    }

    @Override
    public String abrirViagem(RequisicaoId requisicao, String numeroGuia) {
        return abrirViagem.executar(requisicao.valor(), numeroGuia).id();
    }
}

package br.cesar.rotavital.requisicao.dados;

import java.util.List;

import org.springframework.stereotype.Component;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.expedicao.negocio.aplicacao.MontarRemessa;
import br.cesar.rotavital.requisicao.negocio.dominio.ExpedicaoGateway;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.UnidadeSolicitanteId;

/** Adaptador da porta {@link ExpedicaoGateway}: envia o comando MontarRemessa e devolve o número da guia. */
@Component
class ExpedicaoClient implements ExpedicaoGateway {

    private final MontarRemessa montarRemessa;

    ExpedicaoClient(MontarRemessa montarRemessa) {
        this.montarRemessa = montarRemessa;
    }

    @Override
    public String montarRemessa(RequisicaoId requisicao, UnidadeSolicitanteId destino, List<CodigoBolsa> bolsas) {
        return montarRemessa.executar(requisicao.valor(), destino.codigo(),
                bolsas.stream().map(CodigoBolsa::valor).toList()).guia();
    }
}

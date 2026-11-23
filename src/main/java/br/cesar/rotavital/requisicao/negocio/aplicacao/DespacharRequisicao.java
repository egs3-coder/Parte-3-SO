package br.cesar.rotavital.requisicao.negocio.aplicacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoRepository;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusRequisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.TransporteGateway;

/** Passo 5 da saga (ponto sem volta): mandar o Transporte abrir a viagem. */
@Service
public class DespacharRequisicao {

    private final RequisicaoRepository requisicoes;
    private final TransporteGateway transporte;

    public DespacharRequisicao(RequisicaoRepository requisicoes, TransporteGateway transporte) {
        this.requisicoes = requisicoes;
        this.transporte = transporte;
    }

    @Transactional
    public RequisicaoResumo executar(String requisicaoId) {
        Requisicao requisicao =
                ConsultarRequisicoes.carregarParaAtualizar(requisicoes, new RequisicaoId(requisicaoId));
        if (requisicao.status() != StatusRequisicao.ATENDIDA || requisicao.guiaRemessa() == null) {
            throw new RegraDeNegocioException(Requisicao.DESPACHO_SEM_REMESSA);
        }
        String viagem = transporte.abrirViagem(requisicao.id(), requisicao.guiaRemessa());
        requisicao.iniciarTransporte(viagem);
        requisicoes.salvar(requisicao);
        return RequisicaoResumo.de(requisicao);
    }
}

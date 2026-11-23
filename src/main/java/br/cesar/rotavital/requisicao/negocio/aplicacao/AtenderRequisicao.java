package br.cesar.rotavital.requisicao.negocio.aplicacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.requisicao.negocio.dominio.ExpedicaoGateway;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoRepository;

/** Passos 3 e 4 da saga: marcar ATENDIDA (todos os itens completos) e mandar a Expedição montar a remessa. */
@Service
public class AtenderRequisicao {

    private final RequisicaoRepository requisicoes;
    private final ExpedicaoGateway expedicao;

    public AtenderRequisicao(RequisicaoRepository requisicoes, ExpedicaoGateway expedicao) {
        this.requisicoes = requisicoes;
        this.expedicao = expedicao;
    }

    @Transactional
    public RequisicaoResumo executar(String requisicaoId) {
        Requisicao requisicao =
                ConsultarRequisicoes.carregarParaAtualizar(requisicoes, new RequisicaoId(requisicaoId));
        requisicao.marcarAtendida();
        String guia = expedicao.montarRemessa(requisicao.id(), requisicao.solicitante(), requisicao.bolsasAlocadas());
        requisicao.registrarRemessa(guia);
        requisicoes.salvar(requisicao);
        return RequisicaoResumo.de(requisicao);
    }
}

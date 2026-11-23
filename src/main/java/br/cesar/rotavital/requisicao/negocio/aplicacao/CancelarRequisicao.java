package br.cesar.rotavital.requisicao.negocio.aplicacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.requisicao.negocio.dominio.EstoqueGateway;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoRepository;

/** Cancelamento antes do transporte; libera no Estoque as bolsas que estavam alocadas (compensação). */
@Service
public class CancelarRequisicao {

    private final RequisicaoRepository requisicoes;
    private final EstoqueGateway estoque;

    public CancelarRequisicao(RequisicaoRepository requisicoes, EstoqueGateway estoque) {
        this.requisicoes = requisicoes;
        this.estoque = estoque;
    }

    @Transactional
    public RequisicaoResumo executar(String requisicaoId, String motivo) {
        Requisicao requisicao =
                ConsultarRequisicoes.carregarParaAtualizar(requisicoes, new RequisicaoId(requisicaoId));
        requisicao.cancelar(motivo).forEach(codigo -> estoque.liberar(codigo, requisicao.id()));
        requisicoes.salvar(requisicao);
        return RequisicaoResumo.de(requisicao);
    }
}

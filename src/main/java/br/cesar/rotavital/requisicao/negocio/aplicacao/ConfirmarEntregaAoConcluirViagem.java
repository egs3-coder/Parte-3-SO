package br.cesar.rotavital.requisicao.negocio.aplicacao;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoRepository;
import br.cesar.rotavital.transporte.negocio.aplicacao.EntregaConcluida;

/**
 * Consumidor do evento {@link EntregaConcluida} (publicado pelo Transporte): a caixa chegou, então a requisição
 * é encerrada como ENTREGUE. Roda na mesma transação de quem publicou.
 */
@Component
public class ConfirmarEntregaAoConcluirViagem {

    private final RequisicaoRepository requisicoes;

    public ConfirmarEntregaAoConcluirViagem(RequisicaoRepository requisicoes) {
        this.requisicoes = requisicoes;
    }

    @EventListener
    public void quandoEntregaConcluida(EntregaConcluida evento) {
        Requisicao requisicao =
                ConsultarRequisicoes.carregarParaAtualizar(requisicoes, new RequisicaoId(evento.requisicaoId()));
        requisicao.confirmarEntrega();
        requisicoes.salvar(requisicao);
    }
}

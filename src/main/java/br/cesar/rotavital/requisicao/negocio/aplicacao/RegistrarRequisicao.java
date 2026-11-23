package br.cesar.rotavital.requisicao.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.requisicao.negocio.dominio.JustificativaClinica;
import br.cesar.rotavital.requisicao.negocio.dominio.NovoItem;
import br.cesar.rotavital.requisicao.negocio.dominio.Prioridade;
import br.cesar.rotavital.requisicao.negocio.dominio.Quantidade;
import br.cesar.rotavital.requisicao.negocio.dominio.RedeGateway;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoRepository;
import br.cesar.rotavital.requisicao.negocio.dominio.UnidadeSolicitanteId;

/** Caso de uso (passo 1 da saga): registrar o pedido do hospital e colocá-lo em atendimento. */
@Service
public class RegistrarRequisicao {

    public record ItemPedido(Hemocomponente hemocomponente, String tipoReceptor, int quantidade) {
    }

    public record NovaRequisicao(String unidadeSolicitante, String prioridade, String justificativa,
                                 List<ItemPedido> itens) {
    }

    private final RequisicaoRepository requisicoes;
    private final RedeGateway rede;
    private final Clock clock;

    public RegistrarRequisicao(RequisicaoRepository requisicoes, RedeGateway rede, Clock clock) {
        this.requisicoes = requisicoes;
        this.rede = rede;
        this.clock = clock;
    }

    @Transactional
    public RequisicaoResumo executar(NovaRequisicao pedido) {
        UnidadeSolicitanteId unidade = new UnidadeSolicitanteId(pedido.unidadeSolicitante());
        if (!rede.unidadeAtiva(unidade)) {
            throw new RegraDeNegocioException("Unidade solicitante não encontrada ou inativa: " + unidade);
        }
        List<NovoItem> itens = pedido.itens() == null ? List.of() : pedido.itens().stream()
                .map(i -> new NovoItem(i.hemocomponente(), TipoSanguineo.deSimbolo(i.tipoReceptor()),
                        new Quantidade(i.quantidade())))
                .toList();

        Requisicao requisicao = Requisicao.registrar(requisicoes.proximoId(), unidade,
                Prioridade.deNome(pedido.prioridade()), new JustificativaClinica(pedido.justificativa()), itens,
                LocalDateTime.now(clock));
        requisicao.iniciarAtendimento();
        requisicoes.salvar(requisicao);
        return RequisicaoResumo.de(requisicao);
    }
}

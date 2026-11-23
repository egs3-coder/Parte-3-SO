package br.cesar.rotavital.requisicao.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;
import br.cesar.rotavital.requisicao.negocio.dominio.BolsaReservada;
import br.cesar.rotavital.requisicao.negocio.dominio.EstoqueGateway;
import br.cesar.rotavital.requisicao.negocio.dominio.ItemId;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoRepository;

/**
 * Caso de uso da história âncora (Parte 2, B.2): a atendente aloca uma bolsa a um item.
 * O caso de uso só orquestra; quem decide se a bolsa serve é o agregado {@link Requisicao}.
 *
 * <ol>
 *   <li>pede a reserva ao Estoque (que recusa bolsa já reservada ou vencida);</li>
 *   <li>entrega a bolsa reservada ao agregado, que verifica I1, I2 e I3;</li>
 *   <li>se o agregado recusar, libera a reserva (compensação) e repassa a recusa; nada fica alterado.</li>
 * </ol>
 */
@Service
public class AlocarBolsa {

    private final RequisicaoRepository requisicoes;
    private final EstoqueGateway estoque;
    private final Clock clock;

    public AlocarBolsa(RequisicaoRepository requisicoes, EstoqueGateway estoque, Clock clock) {
        this.requisicoes = requisicoes;
        this.estoque = estoque;
        this.clock = clock;
    }

    @Transactional
    public RequisicaoResumo executar(String requisicaoId, String itemId, String codigoBolsa) {
        RequisicaoId id = new RequisicaoId(requisicaoId);
        Requisicao requisicao = ConsultarRequisicoes.carregarParaAtualizar(requisicoes, id);
        ItemId item = new ItemId(itemId);
        CodigoBolsa codigo = new CodigoBolsa(codigoBolsa);

        BolsaReservada bolsa = estoque.reservar(codigo, id);
        try {
            requisicao.alocar(item, bolsa, LocalDateTime.now(clock));
        } catch (RegraDeNegocioException recusa) {
            estoque.liberar(codigo, id);
            throw recusa;
        }
        requisicoes.salvar(requisicao);
        return RequisicaoResumo.de(requisicao);
    }
}

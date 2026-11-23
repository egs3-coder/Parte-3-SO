package br.cesar.rotavital.requisicao.negocio.aplicacao;

import java.time.LocalDateTime;
import java.util.List;

import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.requisicao.negocio.dominio.AlocacaoBolsa;
import br.cesar.rotavital.requisicao.negocio.dominio.ItemRequisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;

/** Como a Requisição se mostra para fora (apresentação, painel e testes de aceitação). */
public record RequisicaoResumo(String id, String unidadeSolicitante, String prioridade, String justificativa,
                               LocalDateTime registradaEm, LocalDateTime entregaPrevista, String status,
                               boolean completa, List<ItemResumo> itens, String guiaRemessa, String viagemId,
                               String motivoCancelamento) {

    public record ItemResumo(String id, Hemocomponente hemocomponente, String tipoReceptor, int quantidade,
                             int alocadas, List<AlocacaoResumo> bolsas) {
    }

    public record AlocacaoResumo(String codigo, String tipoSanguineo, LocalDateTime validade,
                                 LocalDateTime alocadaEm) {
    }

    public ItemResumo item(String itemId) {
        return itens.stream().filter(i -> i.id().equals(itemId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item " + itemId + " não existe em " + id));
    }

    static RequisicaoResumo de(Requisicao r) {
        return new RequisicaoResumo(r.id().valor(), r.solicitante().codigo(), r.prioridade().name(),
                r.justificativa().texto(), r.registradaEm(), r.prioridade().entregaPrevista(r.registradaEm()),
                r.status().name(), r.completa(), r.itens().stream().map(RequisicaoResumo::item).toList(),
                r.guiaRemessa(), r.viagemId(), r.motivoCancelamento());
    }

    private static ItemResumo item(ItemRequisicao i) {
        return new ItemResumo(i.id().valor(), i.hemocomponente(), i.tipoReceptor().simbolo(), i.quantidade().valor(),
                i.quantidadeAlocada(), i.alocacoes().stream().map(RequisicaoResumo::alocacao).toList());
    }

    private static AlocacaoResumo alocacao(AlocacaoBolsa a) {
        return new AlocacaoResumo(a.codigo().valor(), a.tipoSanguineo().simbolo(), a.validade().limite(),
                a.alocadaEm());
    }
}

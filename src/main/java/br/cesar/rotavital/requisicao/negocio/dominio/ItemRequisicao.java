package br.cesar.rotavital.requisicao.negocio.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;

/**
 * Entidade do agregado: um item pedido (hemocomponente, tipo do receptor e quantidade), que pode ser atendido
 * parcialmente. Os métodos que mudam estado são de pacote: só a raiz {@link Requisicao} os chama.
 */
public final class ItemRequisicao {

    private final ItemId id;
    private final Hemocomponente hemocomponente;
    private final TipoSanguineo tipoReceptor;
    private final Quantidade quantidade;
    private final List<AlocacaoBolsa> alocacoes;

    private ItemRequisicao(ItemId id, Hemocomponente hemocomponente, TipoSanguineo tipoReceptor,
                           Quantidade quantidade, List<AlocacaoBolsa> alocacoes) {
        this.id = Objects.requireNonNull(id);
        this.hemocomponente = Objects.requireNonNull(hemocomponente, "Item precisa de hemocomponente");
        this.tipoReceptor = Objects.requireNonNull(tipoReceptor, "Item precisa do tipo do receptor");
        this.quantidade = Objects.requireNonNull(quantidade, "Item precisa de quantidade");
        this.alocacoes = new ArrayList<>(alocacoes);
    }

    static ItemRequisicao novo(ItemId id, NovoItem pedido) {
        return new ItemRequisicao(id, pedido.hemocomponente(), pedido.tipoReceptor(), pedido.quantidade(), List.of());
    }

    /** Reconstrói um item gravado (camada de dados). */
    public static ItemRequisicao reconstituir(ItemId id, Hemocomponente hemocomponente, TipoSanguineo tipoReceptor,
                                              Quantidade quantidade, List<AlocacaoBolsa> alocacoes) {
        return new ItemRequisicao(id, hemocomponente, tipoReceptor, quantidade, alocacoes);
    }

    void adicionar(AlocacaoBolsa alocacao) {
        alocacoes.add(alocacao);
    }

    boolean remover(CodigoBolsa codigo) {
        return alocacoes.removeIf(a -> a.codigo().equals(codigo));
    }

    boolean contem(CodigoBolsa codigo) {
        return alocacoes.stream().anyMatch(a -> a.codigo().equals(codigo));
    }

    public boolean completo() {
        return quantidadeAlocada() >= quantidade.valor();
    }

    public int quantidadeAlocada() {
        return alocacoes.size();
    }

    public ItemId id() {
        return id;
    }

    public Hemocomponente hemocomponente() {
        return hemocomponente;
    }

    public TipoSanguineo tipoReceptor() {
        return tipoReceptor;
    }

    public Quantidade quantidade() {
        return quantidade;
    }

    public List<AlocacaoBolsa> alocacoes() {
        return Collections.unmodifiableList(alocacoes);
    }
}

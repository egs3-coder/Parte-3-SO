package br.cesar.rotavital.requisicao.negocio.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;

/**
 * Raiz do agregado Requisição (contexto core). É a única porta de entrada: itens e alocações só mudam por aqui,
 * e toda operação que quebraria uma invariante é recusada sem alterar nada.
 *
 * <ul>
 *   <li>I1 compatibilidade: mesmo hemocomponente do item e tipo compatível pela regra desse hemocomponente;</li>
 *   <li>I2 validade: a bolsa precisa estar válida na entrega prevista (agora + prazo da prioridade);</li>
 *   <li>I3 quantidade: item nunca recebe mais do que pediu; ao menos um item; bolsa não se repete;</li>
 *   <li>I4 emergência exige justificativa clínica;</li>
 *   <li>I5 ciclo de vida: REGISTRADA → EM_ATENDIMENTO → ATENDIDA → EM_TRANSPORTE → ENTREGUE (ou CANCELADA).</li>
 * </ul>
 */
public final class Requisicao {

    public static final String SEM_ITENS = "A requisição precisa de ao menos um item";
    public static final String EMERGENCIA_SEM_JUSTIFICATIVA = "Requisição de emergência exige justificativa clínica";
    public static final String NAO_ESTA_EM_ATENDIMENTO = "Só é possível alocar bolsas a uma requisição em atendimento";
    public static final String ITEM_INEXISTENTE = "Item não encontrado na requisição";
    public static final String BOLSA_REPETIDA = "Bolsa já alocada nesta requisição";
    public static final String HEMOCOMPONENTE_DIFERENTE = "Hemocomponente diferente do pedido";
    public static final String TIPO_INCOMPATIVEL = "Tipo sanguíneo incompatível";
    public static final String VENCE_ANTES_DA_ENTREGA = "Bolsa vence antes da entrega prevista";
    public static final String ITEM_COMPLETO = "Item já está completo";
    public static final String ITENS_INCOMPLETOS = "Requisição só fica atendida com todos os itens completos";
    public static final String DESPACHO_SEM_REMESSA = "Só se despacha requisição atendida com remessa montada";
    public static final String ENTREGA_FORA_DO_TRANSPORTE = "Só se confirma a entrega de requisição em transporte";
    public static final String NAO_CANCELAVEL = "Requisição em transporte ou encerrada não pode ser cancelada";
    public static final String CANCELAMENTO_SEM_MOTIVO = "Cancelamento exige motivo";
    public static final String TRANSICAO_INVALIDA = "Transição de status inválida";

    private final RequisicaoId id;
    private final UnidadeSolicitanteId solicitante;
    private final Prioridade prioridade;
    private final JustificativaClinica justificativa;
    private final LocalDateTime registradaEm;
    private final List<ItemRequisicao> itens;
    private StatusRequisicao status;
    private String guiaRemessa;
    private String viagemId;
    private String motivoCancelamento;

    private Requisicao(RequisicaoId id, UnidadeSolicitanteId solicitante, Prioridade prioridade,
                       JustificativaClinica justificativa, LocalDateTime registradaEm, List<ItemRequisicao> itens,
                       StatusRequisicao status, String guiaRemessa, String viagemId, String motivoCancelamento) {
        this.id = Objects.requireNonNull(id, "Requisição precisa de id");
        this.solicitante = Objects.requireNonNull(solicitante, "Requisição precisa da unidade solicitante");
        this.prioridade = Objects.requireNonNull(prioridade, "Requisição precisa de prioridade");
        this.justificativa = justificativa == null ? JustificativaClinica.nenhuma() : justificativa;
        this.registradaEm = Objects.requireNonNull(registradaEm);
        this.itens = new ArrayList<>(itens);
        this.status = Objects.requireNonNull(status);
        this.guiaRemessa = guiaRemessa;
        this.viagemId = viagemId;
        this.motivoCancelamento = motivoCancelamento;
    }

    /** Registra o pedido do hospital. Os itens recebem ITEM-1, ITEM-2... na ordem em que vieram. */
    public static Requisicao registrar(RequisicaoId id, UnidadeSolicitanteId solicitante, Prioridade prioridade,
                                       JustificativaClinica justificativa, List<NovoItem> pedidos,
                                       LocalDateTime agora) {
        JustificativaClinica texto = justificativa == null ? JustificativaClinica.nenhuma() : justificativa;
        List<ItemRequisicao> itens = new ArrayList<>();
        for (int i = 0; i < pedidos.size(); i++) {
            itens.add(ItemRequisicao.novo(ItemId.daPosicao(i + 1), pedidos.get(i)));
        }
        return new Requisicao(id, solicitante, prioridade, texto, agora, itens, StatusRequisicao.REGISTRADA,
                null, null, null);
    }

    /** Reconstrói uma requisição gravada (camada de dados). */
    public static Requisicao reconstituir(RequisicaoId id, UnidadeSolicitanteId solicitante, Prioridade prioridade,
                                          JustificativaClinica justificativa, LocalDateTime registradaEm,
                                          List<ItemRequisicao> itens, StatusRequisicao status, String guiaRemessa,
                                          String viagemId, String motivoCancelamento) {
        return new Requisicao(id, solicitante, prioridade, justificativa, registradaEm, itens, status, guiaRemessa,
                viagemId, motivoCancelamento);
    }

    /** Passo 1 da saga: a atendente assume o pedido. */
    public void iniciarAtendimento() {
        status = StatusRequisicao.EM_ATENDIMENTO;
    }

    /**
     * Aloca uma bolsa reservada no Estoque a um item. A primeira regra quebrada recusa a operação e nada muda.
     */
    public void alocar(ItemId itemId, BolsaReservada bolsa, LocalDateTime agora) {
        // TODO verde: invariantes I1, I2, I3 e I5
        ItemRequisicao item = item(itemId).orElseThrow();
        item.adicionar(AlocacaoBolsa.de(bolsa, agora));
    }

    /** Compensação: tira a bolsa do item (ex.: a Expedição achou a embalagem rompida). */
    public void desalocar(ItemId itemId, CodigoBolsa codigo) {
        item(itemId).orElseThrow().remover(codigo);
    }

    /** I5: só fica ATENDIDA com todos os itens completos. */
    public void marcarAtendida() {
        // TODO verde: só com todos os itens completos (I5)
        status = StatusRequisicao.ATENDIDA;
    }

    /** Guarda a guia devolvida pela Expedição (resposta RemessaMontada). */
    public void registrarRemessa(String numeroGuia) {
        guiaRemessa = numeroGuia.trim();
    }

    /** Ponto sem volta da saga: o Transporte abriu a viagem. */
    public void iniciarTransporte(String idViagem) {
        viagemId = idViagem;
        status = StatusRequisicao.EM_TRANSPORTE;
    }

    /** O hospital recebeu: encerra a requisição. */
    public void confirmarEntrega() {
        status = StatusRequisicao.ENTREGUE;
    }

    /** Cancela antes do transporte e devolve as bolsas alocadas, para o Estoque liberar (compensação). */
    public List<CodigoBolsa> cancelar(String motivo) {
        motivoCancelamento = motivo;
        status = StatusRequisicao.CANCELADA;
        return bolsasAlocadas();
    }

    // ---------- leitura ----------

    public boolean completa() {
        return itens.stream().allMatch(ItemRequisicao::completo);
    }

    public Optional<ItemRequisicao> item(ItemId itemId) {
        return itens.stream().filter(i -> i.id().equals(itemId)).findFirst();
    }

    public List<CodigoBolsa> bolsasAlocadas() {
        return itens.stream().flatMap(i -> i.alocacoes().stream()).map(AlocacaoBolsa::codigo).toList();
    }

    public RequisicaoId id() {
        return id;
    }

    public UnidadeSolicitanteId solicitante() {
        return solicitante;
    }

    public Prioridade prioridade() {
        return prioridade;
    }

    public JustificativaClinica justificativa() {
        return justificativa;
    }

    public LocalDateTime registradaEm() {
        return registradaEm;
    }

    public List<ItemRequisicao> itens() {
        return Collections.unmodifiableList(itens);
    }

    public StatusRequisicao status() {
        return status;
    }

    public String guiaRemessa() {
        return guiaRemessa;
    }

    public String viagemId() {
        return viagemId;
    }

    public String motivoCancelamento() {
        return motivoCancelamento;
    }
}

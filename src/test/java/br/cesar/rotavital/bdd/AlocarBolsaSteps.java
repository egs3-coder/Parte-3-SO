package br.cesar.rotavital.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.compartilhado.dominio.Validade;
import br.cesar.rotavital.requisicao.negocio.aplicacao.AlocarBolsa;
import br.cesar.rotavital.requisicao.negocio.aplicacao.ConsultarRequisicoes;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RegistrarRequisicao;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RegistrarRequisicao.ItemPedido;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RegistrarRequisicao.NovaRequisicao;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RequisicaoResumo;
import br.cesar.rotavital.requisicao.negocio.dominio.BolsaReservada;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/**
 * Passos de {@code features/alocar_bolsa.feature}. Cada cenário tem uma instância nova desta classe
 * (estado limpo). Os casos de uso são os mesmos que a API usa; só os adaptadores são em memória.
 */
public class AlocarBolsaSteps {

    private static final ZoneId FUSO = ZoneId.of("America/Recife");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AdaptadoresEmMemoria.Requisicoes requisicoes = new AdaptadoresEmMemoria.Requisicoes();
    private final AdaptadoresEmMemoria.Estoque estoque = new AdaptadoresEmMemoria.Estoque();
    private final AdaptadoresEmMemoria.Rede rede = new AdaptadoresEmMemoria.Rede();

    private Clock relogio = Clock.system(FUSO);

    private String idEsperado;
    private String prioridade;
    private String unidade;
    private final List<ItemPedido> itens = new ArrayList<>();
    private final List<String> idsDosItens = new ArrayList<>();
    private RequisicaoResumo requisicao;

    private String recusa;
    private boolean alocou;

    // ---------- Dado ----------

    @Dado("que agora são {string}")
    public void agoraSao(String dataHora) {
        LocalDateTime agora = LocalDateTime.parse(dataHora, DATA_HORA);
        relogio = Clock.fixed(agora.atZone(FUSO).toInstant(), FUSO);
    }

    @Dado("existe a requisição {string} de prioridade {string} do {string}")
    public void existeARequisicao(String id, String prioridade, String unidade) {
        this.idEsperado = id;
        this.prioridade = prioridade;
        this.unidade = rede.cadastrar(unidade).codigo();
    }

    @Dado("a requisição tem o item {string} de {string} para receptor {string} com quantidade {int}")
    public void temOItem(String itemId, String hemocomponente, String receptor, int quantidade) {
        itens.add(new ItemPedido(Hemocomponente.deNome(hemocomponente), receptor, quantidade));
        idsDosItens.add(itemId);
    }

    // ---------- Quando ----------

    @Quando("a atendente alocar a bolsa {string} de {string} do tipo {string} com validade {string} ao item {string}")
    public void alocar(String codigo, String hemocomponente, String tipo, String validade, String itemId) {
        garantirRequisicaoRegistrada();
        estoque.adicionar(new BolsaReservada(new CodigoBolsa(codigo), Hemocomponente.deNome(hemocomponente),
                TipoSanguineo.deSimbolo(tipo), new Validade(LocalDateTime.parse(validade, DATA_HORA))));
        try {
            requisicao = new AlocarBolsa(requisicoes, estoque, relogio).executar(idEsperado, itemId, codigo);
            alocou = true;
        } catch (RegraDeNegocioException e) {
            recusa = e.getMessage();
        }
    }

    // ---------- Então ----------

    @Então("a alocação é aceita")
    public void aceita() {
        assertThat(recusa).as("motivo da recusa").isNull();
        assertThat(alocou).isTrue();
    }

    @Então("a alocação é recusada com a mensagem {string}")
    public void recusada(String mensagem) {
        assertThat(alocou).as("a alocação deveria ter sido recusada").isFalse();
        assertThat(recusa).isEqualTo(mensagem);
    }

    @Então("o item {string} tem {int} bolsa(s) alocada(s)")
    public void itemTem(String itemId, int quantidade) {
        assertThat(consultar().item(itemId).alocadas()).isEqualTo(quantidade);
    }

    @Então("a requisição {string} continua com status {string}")
    public void continuaComStatus(String id, String status) {
        assertThat(consultar().id()).isEqualTo(id);
        assertThat(consultar().status()).isEqualTo(status);
        // a recusa também não deixa reserva no Estoque: reservadas = alocadas
        List<String> alocadas = consultar().itens().stream()
                .flatMap(item -> item.bolsas().stream().map(RequisicaoResumo.AlocacaoResumo::codigo)).toList();
        assertThat(estoque.reservadas()).containsExactlyInAnyOrderElementsOf(alocadas);
    }

    // ---------- auxiliares ----------

    /** Registra a requisição (pelo caso de uso) na primeira ação do cenário, com os itens do Contexto. */
    private void garantirRequisicaoRegistrada() {
        if (requisicao != null) {
            return;
        }
        requisicao = new RegistrarRequisicao(requisicoes, rede, relogio)
                .executar(new NovaRequisicao(unidade, prioridade, null, itens));
        assertThat(requisicao.id()).isEqualTo(idEsperado);
        assertThat(requisicao.itens()).extracting(RequisicaoResumo.ItemResumo::id).containsExactlyElementsOf(idsDosItens);
    }

    private RequisicaoResumo consultar() {
        return new ConsultarRequisicoes(requisicoes).buscar(idEsperado);
    }
}

package br.cesar.rotavital.aceitacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import br.cesar.rotavital.captacao.negocio.dominio.Doacao;
import br.cesar.rotavital.captacao.negocio.dominio.DoacaoRepository;
import br.cesar.rotavital.captacao.negocio.dominio.DoadorRepository;
import br.cesar.rotavital.captacao.negocio.dominio.ResultadoTriagem;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.estoque.negocio.aplicacao.RegistrarBolsa;
import br.cesar.rotavital.estoque.negocio.aplicacao.RegistrarBolsa.NovaBolsa;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos dos cenários de aceitação: falam com a API por HTTP, como um cliente de verdade. */
public class RotaVitalSteps {

    /** Tabelas de todos os contextos, limpas antes de cada cenário. */
    static final String TABELAS = String.join(", ",
            "captacao.doacao", "captacao.doador", "estoque.bolsa", "rede.unidade",
            "requisicao.alocacao", "requisicao.item", "requisicao.requisicao",
            "expedicao.remessa_bolsa", "expedicao.remessa", "transporte.leitura", "transporte.viagem");

    /** CPFs válidos (dígitos verificadores corretos), um por doador criado nos cenários. */
    private static final AtomicLong SEQUENCIA_CPF = new AtomicLong(100_000_000L);

    private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {
    };

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private Clock clock;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DoadorRepository doadores;

    @Autowired
    private DoacaoRepository doacoes;

    @Autowired
    private RegistrarBolsa registrarBolsa;

    private final Map<String, Long> idPorNome = new HashMap<>();
    private final Map<String, String> bolsaDoadaPor = new HashMap<>();
    private ResponseEntity<Map<String, Object>> ultimaResposta;
    private String requisicaoId;
    private String viagemId;

    @Before
    public void limparBanco() {
        jdbc.execute("TRUNCATE " + TABELAS + " RESTART IDENTITY CASCADE");
        idPorNome.clear();
        bolsaDoadaPor.clear();
        requisicaoId = null;
        viagemId = null;
    }

    LocalDate hoje() {
        return LocalDate.now(clock);
    }

    // ---------- Captação ----------

    @Dado("um doador {string} do sexo {string} com {int} anos, {int} kg e tipo {string}")
    public void umDoador(String nome, String sexo, int idade, int peso, String tipo) {
        Map<String, Object> corpo = Map.of(
                "nome", nome,
                "cpf", cpfValido(SEQUENCIA_CPF.incrementAndGet()),
                "dataNascimento", hoje().minusYears(idade).minusDays(1).toString(),
                "sexo", sexo,
                "pesoKg", peso,
                "tipoSanguineo", tipo);

        var resposta = http.exchange("/doadores", HttpMethod.POST, new HttpEntity<>(corpo), JSON);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idPorNome.put(nome, ((Number) resposta.getBody().get("id")).longValue());
    }

    /** Doação antiga: grava direto pelas portas (sem HTTP), e a bolsa entra no Estoque pelo caso de uso. */
    @Dado("{string} doou sangue há {int} dias")
    public void doouHaDias(String nome, int dias) {
        var doador = doadores.buscar(idPorNome.get(nome)).orElseThrow();
        LocalDate data = hoje().minusDays(dias);
        var doacao = doacoes.salvar(
                Doacao.registrar(doador, ResultadoTriagem.de(List.of()), doacoes.proximoNumero(), data));
        registrarBolsa.executar(new NovaBolsa(doacao.numero().valor(), Hemocomponente.CONCENTRADO_HEMACIAS,
                doador.tipoSanguineo().simbolo(), data));
    }

    @Quando("consulto a triagem de {string}")
    public void consultoTriagem(String nome) {
        ultimaResposta = http.exchange("/doadores/" + idPorNome.get(nome) + "/triagem", HttpMethod.GET, null, JSON);
    }

    @Quando("{string} doa sangue hoje")
    public void doaHoje(String nome) {
        ultimaResposta = http.exchange("/doadores/" + idPorNome.get(nome) + "/doacoes", HttpMethod.POST, null, JSON);
        if (ultimaResposta.getStatusCode() == HttpStatus.CREATED) {
            bolsaDoadaPor.put(nome, (String) ultimaResposta.getBody().get("codigoBolsa"));
        }
    }

    @Então("o doador está apto")
    public void doadorApto() {
        assertThat(ultimaResposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ultimaResposta.getBody().get("apto")).isEqualTo(true);
    }

    @Então("o doador está inapto com o motivo {string}")
    public void doadorInapto(String motivo) {
        assertThat(ultimaResposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ultimaResposta.getBody().get("apto")).isEqualTo(false);
        assertThat(ultimaResposta.getBody().get("motivos")).asInstanceOf(InstanceOfAssertFactories.LIST)
                .contains(motivo);
    }

    /** A bolsa é do Estoque: o passo confere pelo endpoint do Estoque que o evento DoacaoRegistrada chegou. */
    @Então("a doação é registrada com bolsa {string} válida por {int} dias")
    public void doacaoRegistrada(String tipo, int dias) {
        assertThat(ultimaResposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(ultimaResposta.getBody().get("tipoSanguineo")).isEqualTo(tipo);
        String codigo = (String) ultimaResposta.getBody().get("codigoBolsa");

        var bolsa = http.exchange("/estoque/bolsas/" + codigo, HttpMethod.GET, null, JSON);
        assertThat(bolsa.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bolsa.getBody().get("tipoSanguineo")).isEqualTo(tipo);
        assertThat(bolsa.getBody().get("status")).isEqualTo("DISPONIVEL");
        assertThat((String) bolsa.getBody().get("validade")).startsWith(hoje().plusDays(dias) + "T23:59");
    }

    @Então("a doação é recusada com o motivo {string}")
    public void doacaoRecusada(String motivo) {
        assertThat(ultimaResposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ultimaResposta.getBody().get("motivos")).asInstanceOf(InstanceOfAssertFactories.LIST)
                .contains(motivo);
    }

    // ---------- Estoque ----------

    @Então("o estoque tem {int} bolsa(s) {string} disponível(is)")
    public void estoqueTem(int quantidade, String tipo) {
        assertThat(estoque().getOrDefault(tipo, 0)).isEqualTo(quantidade);
    }

    private Map<String, Integer> estoque() {
        var resposta = http.exchange("/estoque", HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Integer>>() {
                });
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resposta.getBody();
    }

    @Dado("o estoque recebe a bolsa de plasma {string} do tipo {string} coletada hoje")
    public void estoqueRecebePlasma(String codigo, String tipo) {
        var resposta = post("/estoque/bolsas", Map.of("codigo", codigo, "hemocomponente", "PLASMA_FRESCO_CONGELADO",
                "tipoSanguineo", tipo, "dataColeta", hoje().toString()));
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Então("a bolsa doada por {string} continua {string} no estoque")
    public void bolsaContinua(String doador, String status) {
        var bolsa = http.exchange("/estoque/bolsas/" + bolsaDoadaPor.get(doador), HttpMethod.GET, null, JSON);
        assertThat(bolsa.getBody().get("status")).isEqualTo(status);
        assertThat(bolsa.getBody().get("reservadaPara")).isNull();
    }

    // ---------- Rede ----------

    @Dado("a unidade {string} {string} de {string}")
    public void aUnidade(String codigo, String nome, String cidade) {
        var resposta = post("/rede/unidades", Map.of("codigo", codigo, "nome", nome, "cidade", cidade, "uf", "PE"));
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    // ---------- Requisição (agregado) ----------

    @Dado("a requisição de {string} da unidade {string} com os itens:")
    public void aRequisicao(String prioridade, String unidade, DataTable itens) {
        List<Map<String, Object>> corpoItens = itens.asMaps().stream()
                .map(l -> Map.<String, Object>of("hemocomponente", l.get("hemocomponente"),
                        "tipoReceptor", l.get("receptor"), "quantidade", Integer.parseInt(l.get("quantidade"))))
                .toList();
        var resposta = post("/requisicoes", Map.of("unidadeSolicitante", unidade, "prioridade", prioridade,
                "itens", corpoItens));
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        requisicaoId = (String) resposta.getBody().get("id");
        assertThat(resposta.getBody().get("status")).isEqualTo("EM_ATENDIMENTO");
    }

    @Quando("o hospital pede uma requisição de {string} da unidade {string} sem justificativa")
    public void pedeSemJustificativa(String prioridade, String unidade) {
        ultimaResposta = post("/requisicoes", Map.of("unidadeSolicitante", unidade, "prioridade", prioridade,
                "itens", List.of(Map.of("hemocomponente", "CONCENTRADO_HEMACIAS", "tipoReceptor", "O-",
                        "quantidade", 1))));
    }

    @Quando("a atendente aloca a bolsa doada por {string} ao item {string}")
    public void alocaBolsaDoada(String doador, String item) {
        alocaBolsa(bolsaDoadaPor.get(doador), item);
    }

    @Quando("a atendente aloca a bolsa {string} ao item {string}")
    public void alocaBolsa(String codigo, String item) {
        ultimaResposta = post("/requisicoes/" + requisicaoId + "/itens/" + item + "/alocacoes",
                Map.of("codigoBolsa", codigo));
    }

    @Quando("a atendente marca a requisição como atendida")
    public void marcaAtendida() {
        ultimaResposta = post("/requisicoes/" + requisicaoId + "/atendimento", Map.of());
        assertThat(ultimaResposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Quando("a atendente despacha a requisição")
    public void despacha() {
        ultimaResposta = post("/requisicoes/" + requisicaoId + "/despacho", Map.of());
        assertThat(ultimaResposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        viagemId = (String) ultimaResposta.getBody().get("viagemId");
    }

    @Quando("a atendente cancela a requisição com o motivo {string}")
    public void cancela(String motivo) {
        ultimaResposta = post("/requisicoes/" + requisicaoId + "/cancelamento", Map.of("motivo", motivo));
        assertThat(ultimaResposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Então("a requisição fica com status {string}")
    public void requisicaoFica(String status) {
        var resposta = http.exchange("/requisicoes/" + requisicaoId, HttpMethod.GET, null, JSON);
        assertThat(resposta.getBody().get("status")).isEqualTo(status);
    }

    @Então("a API recusa com {int} e a mensagem {string}")
    public void apiRecusa(int status, String mensagem) {
        assertThat(ultimaResposta.getStatusCode().value()).isEqualTo(status);
        assertThat(ultimaResposta.getBody().get("detail")).isEqualTo(mensagem);
    }

    // ---------- Expedição e Transporte ----------

    @Então("existe a remessa da requisição com {int} bolsas")
    public void existeRemessa(int bolsas) {
        var remessa = http.exchange("/remessas/GR-" + requisicaoId, HttpMethod.GET, null, JSON);
        assertThat(remessa.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(remessa.getBody().get("bolsas")).asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(bolsas);
    }

    @Quando("o sensor da caixa envia {int} °F")
    public void sensorEnvia(int fahrenheit) {
        var resposta = post("/viagens/" + viagemId + "/leituras", Map.of("temperaturaF", fahrenheit));
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Então("a viagem registrou {int} leituras e {int} excursão de temperatura")
    public void viagemRegistrou(int leituras, int excursoes) {
        var viagem = http.exchange("/viagens/" + viagemId, HttpMethod.GET, null, JSON);
        assertThat(((Number) viagem.getBody().get("leituras")).intValue()).isEqualTo(leituras);
        assertThat(((Number) viagem.getBody().get("excursoes")).intValue()).isEqualTo(excursoes);
    }

    @Quando("o motorista confirma a entrega")
    public void confirmaEntrega() {
        var resposta = post("/viagens/" + viagemId + "/entrega", Map.of());
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ---------- auxiliares ----------

    private ResponseEntity<Map<String, Object>> post(String caminho, Map<String, ?> corpo) {
        return http.exchange(caminho, HttpMethod.POST, new HttpEntity<>(corpo), JSON);
    }

    /** Monta um CPF com dígitos verificadores corretos a partir de 9 dígitos. */
    static String cpfValido(long base) {
        String nove = String.format("%09d", base % 1_000_000_000L);
        String dez = nove + digito(nove);
        return dez + digito(dez);
    }

    private static int digito(String parcial) {
        int soma = 0;
        int peso = parcial.length() + 1;
        for (char c : parcial.toCharArray()) {
            soma += (c - '0') * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}

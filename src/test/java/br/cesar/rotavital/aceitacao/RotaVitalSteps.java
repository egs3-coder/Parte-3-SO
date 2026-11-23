package br.cesar.rotavital.aceitacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
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
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Passos dos cenários de aceitação: falam com a API por HTTP, como um cliente de verdade. */
public class RotaVitalSteps {

    /** Tabelas de todos os contextos, limpas antes de cada cenário. */
    static final String TABELAS = String.join(", ",
            "captacao.doacao", "captacao.doador", "estoque.bolsa", "rede.unidade",
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
    private ResponseEntity<Map<String, Object>> ultimaResposta;

    @Before
    public void limparBanco() {
        jdbc.execute("TRUNCATE " + TABELAS + " RESTART IDENTITY CASCADE");
        idPorNome.clear();
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
                Doacao.registrar(doador, ResultadoTriagem.de(java.util.List.of()), doacoes.proximoNumero(), data));
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

    // ---------- auxiliares ----------

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

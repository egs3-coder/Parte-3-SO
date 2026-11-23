package br.cesar.rotavital.integracao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import br.cesar.rotavital.TestcontainersConfiguration;

/** Página web e rotas de leitura usadas por ela, pela esteira e pelo teste de fumaça. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.locations=classpath:db/migration")
@Import(TestcontainersConfiguration.class)
class PortalIT {

    @Autowired
    TestRestTemplate http;

    @Test
    @DisplayName("GET / devolve a página HTML")
    void raizMostraAPagina() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.TEXT_HTML));
        ResponseEntity<String> resposta = http.exchange("/", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody()).contains("<title>Rota Vital");
    }

    @Test
    @DisplayName("GET /api/info informa a versão (usado no deploy, no rollback e no teste de fumaça)")
    @SuppressWarnings("rawtypes")
    void infoTemVersao() {
        Map corpo = http.getForObject("/api/info", Map.class);

        assertThat(corpo).containsKeys("servico", "versao", "contextos");
        assertThat(corpo.get("versao")).isEqualTo("local");
    }

    @Test
    @DisplayName("GET /api/resumo traz doadores, doações e bolsas utilizáveis")
    @SuppressWarnings("rawtypes")
    void resumo() {
        ResponseEntity<Map> resposta = http.getForEntity("/api/resumo", Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody()).containsKeys("doadores", "doacoes", "bolsasUtilizaveis", "estoque");
    }

    @Test
    @DisplayName("erro de regra vira Problem Details: CPF inválido = 400")
    @SuppressWarnings("rawtypes")
    void cpfInvalido() {
        Map<String, Object> corpo = Map.of("nome", "Teste", "cpf", "52998224724", "dataNascimento", "1990-01-01",
                "sexo", "FEMININO", "pesoKg", 60, "tipoSanguineo", "O+");
        ResponseEntity<Map> resposta = http.postForEntity("/doadores", corpo, Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) resposta.getBody().get("detail")).contains("CPF inválido");
    }
}

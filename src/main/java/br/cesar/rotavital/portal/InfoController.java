package br.cesar.rotavital.portal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Versão publicada (SHA do commit) e rotas. A esteira confere aqui se o deploy e o rollback aconteceram
 * e o teste de fumaça confere a versão.
 */
@RestController
public class InfoController {

    private final String versao;

    public InfoController(@Value("${APP_VERSION:local}") String versao) {
        this.versao = versao;
    }

    @GetMapping("/api/info")
    public Map<String, Object> info() {
        Map<String, Object> contextos = new LinkedHashMap<>();
        contextos.put("captacao", Map.of(
                "POST /doadores", "cadastrar doador",
                "GET /doadores/{id}/triagem", "triagem (H1)",
                "POST /doadores/{id}/doacoes", "registrar doação (H2); publica DoacaoRegistrada"));
        contextos.put("estoque", Map.of(
                "GET /estoque", "bolsas utilizáveis por tipo",
                "GET /estoque/criticos", "tipos abaixo do mínimo (T4.1)",
                "GET|POST /estoque/bolsas", "listar e registrar bolsas",
                "POST /estoque/descarte-de-vencidas", "descartar vencidas (T4.2)"));
        contextos.put("requisicao", Map.of(
                "GET|POST /requisicoes", "listar e registrar requisições",
                "POST /requisicoes/{id}/itens/{itemId}/alocacoes", "alocar bolsa (agregado Requisicao)",
                "POST /requisicoes/{id}/atendimento", "marcar atendida e montar remessa",
                "POST /requisicoes/{id}/despacho", "abrir viagem",
                "POST /requisicoes/{id}/cancelamento", "cancelar e liberar bolsas"));
        contextos.put("expedicao", Map.of("GET /remessas", "remessas e guias"));
        contextos.put("transporte", Map.of(
                "GET /viagens", "viagens",
                "POST /viagens/{id}/leituras", "telemetria do sensor (°F → °C)",
                "POST /viagens/{id}/entrega", "chegada; publica EntregaConcluida"));
        contextos.put("rede", Map.of("GET|POST /rede/unidades", "hospitais e unidades"));

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("servico", "Rota Vital");
        info.put("versao", versao);
        info.put("arquitetura", "monolito modular · 6 bounded contexts · 3 camadas por contexto");
        info.put("contextos", contextos);
        return info;
    }
}

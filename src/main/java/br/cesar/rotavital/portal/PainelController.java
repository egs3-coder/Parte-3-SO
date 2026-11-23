package br.cesar.rotavital.portal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.cesar.rotavital.captacao.negocio.aplicacao.ConsultarDoadores;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.estoque.negocio.aplicacao.ConsultarEstoque;

/**
 * Leitura composta para a página inicial. O portal não tem regra nem tabela: só junta o que cada contexto
 * publica na sua camada de aplicação.
 */
@RestController
public class PainelController {

    private final ConsultarDoadores captacao;
    private final ConsultarEstoque estoque;

    public PainelController(ConsultarDoadores captacao, ConsultarEstoque estoque) {
        this.captacao = captacao;
        this.estoque = estoque;
    }

    @GetMapping("/api/resumo")
    public Map<String, Object> resumo() {
        Map<String, Integer> hemacias = estoque.utilizaveisPorTipo(Hemocomponente.CONCENTRADO_HEMACIAS);
        Map<String, Integer> plasma = estoque.utilizaveisPorTipo(Hemocomponente.PLASMA_FRESCO_CONGELADO);
        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("doadores", captacao.totalDeDoadores());
        resumo.put("doacoes", captacao.totalDeDoacoes());
        resumo.put("bolsasUtilizaveis", soma(hemacias) + soma(plasma));
        resumo.put("estoque", hemacias);
        resumo.put("estoquePlasma", plasma);
        return resumo;
    }

    private static int soma(Map<String, Integer> porTipo) {
        return porTipo.values().stream().mapToInt(Integer::intValue).sum();
    }
}

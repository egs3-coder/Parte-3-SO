package br.cesar.rotavital.estoque.apresentacao;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.estoque.negocio.aplicacao.BolsaResumo;
import br.cesar.rotavital.estoque.negocio.aplicacao.ConsultarEstoque;
import br.cesar.rotavital.estoque.negocio.aplicacao.DescartarBolsasVencidas;
import br.cesar.rotavital.estoque.negocio.aplicacao.RegistrarBolsa;
import br.cesar.rotavital.estoque.negocio.aplicacao.RegistrarBolsa.NovaBolsa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

/** Apresentação do Estoque. As rotas /estoque e /estoque/criticos são as mesmas da Parte 1. */
@RestController
public class EstoqueController {

    public record NovaBolsaRequest(@NotBlank String codigo, @NotNull Hemocomponente hemocomponente,
                                   @NotBlank String tipoSanguineo, @NotNull @PastOrPresent LocalDate dataColeta) {
    }

    private final ConsultarEstoque consultar;
    private final RegistrarBolsa registrar;
    private final DescartarBolsasVencidas descartar;

    public EstoqueController(ConsultarEstoque consultar, RegistrarBolsa registrar, DescartarBolsasVencidas descartar) {
        this.consultar = consultar;
        this.registrar = registrar;
        this.descartar = descartar;
    }

    /** Bolsas utilizáveis por tipo (padrão: concentrado de hemácias, como na Parte 1). */
    @GetMapping("/estoque")
    public Map<String, Integer> estoque(
            @RequestParam(defaultValue = "CONCENTRADO_HEMACIAS") Hemocomponente hemocomponente) {
        return consultar.utilizaveisPorTipo(hemocomponente);
    }

    /** T4.1: tipos abaixo do estoque mínimo de segurança. */
    @GetMapping("/estoque/criticos")
    public List<String> criticos() {
        return consultar.tiposCriticos();
    }

    /** Bolsas utilizáveis; com ?todas=true inclui reservadas e vencidas ainda não descartadas. */
    @GetMapping("/estoque/bolsas")
    public List<BolsaResumo> bolsas(@RequestParam(defaultValue = "false") boolean todas) {
        return todas ? consultar.emEstoque() : consultar.utilizaveis();
    }

    @GetMapping("/estoque/bolsas/{codigo}")
    public BolsaResumo bolsa(@PathVariable String codigo) {
        return consultar.buscar(codigo)
                .orElseThrow(() -> new NaoEncontradoException("Bolsa " + codigo + " não encontrada"));
    }

    /** Entrada direta de bolsa (ex.: plasma vindo do fracionamento), com dados sintéticos. */
    @PostMapping("/estoque/bolsas")
    public ResponseEntity<BolsaResumo> registrar(@Valid @RequestBody NovaBolsaRequest r) {
        BolsaResumo bolsa = registrar.executar(new NovaBolsa(r.codigo(), r.hemocomponente(), r.tipoSanguineo(),
                r.dataColeta()));
        return ResponseEntity.created(URI.create("/estoque/bolsas/" + bolsa.codigo())).body(bolsa);
    }

    /** T4.2: descarta as bolsas vencidas e devolve os códigos. */
    @PostMapping("/estoque/descarte-de-vencidas")
    public List<String> descartarVencidas() {
        return descartar.executar();
    }
}

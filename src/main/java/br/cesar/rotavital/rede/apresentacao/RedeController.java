package br.cesar.rotavital.rede.apresentacao;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;
import br.cesar.rotavital.rede.negocio.aplicacao.CadastrarUnidade;
import br.cesar.rotavital.rede.negocio.aplicacao.CadastrarUnidade.NovaUnidade;
import br.cesar.rotavital.rede.negocio.aplicacao.ConsultarUnidades;
import br.cesar.rotavital.rede.negocio.aplicacao.UnidadeResumo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/** Apresentação da Rede: hospitais e unidades solicitantes (dados sintéticos). */
@RestController
public class RedeController {

    public record NovaUnidadeRequest(@NotBlank String codigo, @NotBlank String nome, @NotBlank String cidade,
                                     @NotBlank String uf) {
    }

    private final ConsultarUnidades consultar;
    private final CadastrarUnidade cadastrar;

    public RedeController(ConsultarUnidades consultar, CadastrarUnidade cadastrar) {
        this.consultar = consultar;
        this.cadastrar = cadastrar;
    }

    @GetMapping("/rede/unidades")
    public List<UnidadeResumo> unidades() {
        return consultar.todas();
    }

    @GetMapping("/rede/unidades/{codigo}")
    public UnidadeResumo unidade(@PathVariable String codigo) {
        return consultar.buscar(codigo)
                .orElseThrow(() -> new NaoEncontradoException("Unidade " + codigo + " não encontrada"));
    }

    @PostMapping("/rede/unidades")
    public ResponseEntity<UnidadeResumo> cadastrar(@Valid @RequestBody NovaUnidadeRequest r) {
        UnidadeResumo unidade = cadastrar.executar(new NovaUnidade(r.codigo(), r.nome(), r.cidade(), r.uf()));
        return ResponseEntity.created(URI.create("/rede/unidades/" + unidade.codigo())).body(unidade);
    }
}

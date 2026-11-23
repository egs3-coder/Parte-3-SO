package br.cesar.rotavital.expedicao.apresentacao;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;
import br.cesar.rotavital.expedicao.negocio.aplicacao.ConsultarRemessas;
import br.cesar.rotavital.expedicao.negocio.aplicacao.RemessaResumo;

/** Apresentação da Expedição: remessas montadas e guias. */
@RestController
public class ExpedicaoController {

    private final ConsultarRemessas consultar;

    public ExpedicaoController(ConsultarRemessas consultar) {
        this.consultar = consultar;
    }

    @GetMapping("/remessas")
    public List<RemessaResumo> remessas() {
        return consultar.todas();
    }

    @GetMapping("/remessas/{guia}")
    public RemessaResumo remessa(@PathVariable String guia) {
        return consultar.buscar(guia)
                .orElseThrow(() -> new NaoEncontradoException("Remessa " + guia + " não encontrada"));
    }
}

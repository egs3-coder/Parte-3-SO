package br.cesar.rotavital.requisicao.apresentacao;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.requisicao.negocio.aplicacao.AlocarBolsa;
import br.cesar.rotavital.requisicao.negocio.aplicacao.AtenderRequisicao;
import br.cesar.rotavital.requisicao.negocio.aplicacao.CancelarRequisicao;
import br.cesar.rotavital.requisicao.negocio.aplicacao.ConsultarRequisicoes;
import br.cesar.rotavital.requisicao.negocio.aplicacao.DespacharRequisicao;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RegistrarRequisicao;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RegistrarRequisicao.ItemPedido;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RegistrarRequisicao.NovaRequisicao;
import br.cesar.rotavital.requisicao.negocio.aplicacao.RequisicaoResumo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Apresentação da Requisição: só formato e HTTP. Toda regra (compatibilidade, validade, quantidade, ciclo de vida)
 * fica no agregado; uma recusa volta como 422 com a mensagem da linguagem ubíqua.
 */
@RestController
public class RequisicaoController {

    public record ItemRequest(@NotNull Hemocomponente hemocomponente, @NotBlank String tipoReceptor,
                              @Min(1) @Max(20) int quantidade) {
    }

    public record NovaRequisicaoRequest(@NotBlank String unidadeSolicitante, @NotBlank String prioridade,
                                        String justificativa, @NotEmpty List<@Valid ItemRequest> itens) {
    }

    public record AlocacaoRequest(@NotBlank String codigoBolsa) {
    }

    public record CancelamentoRequest(String motivo) {
    }

    private final RegistrarRequisicao registrar;
    private final AlocarBolsa alocar;
    private final AtenderRequisicao atender;
    private final DespacharRequisicao despachar;
    private final CancelarRequisicao cancelar;
    private final ConsultarRequisicoes consultar;

    public RequisicaoController(RegistrarRequisicao registrar, AlocarBolsa alocar, AtenderRequisicao atender,
                                DespacharRequisicao despachar, CancelarRequisicao cancelar,
                                ConsultarRequisicoes consultar) {
        this.registrar = registrar;
        this.alocar = alocar;
        this.atender = atender;
        this.despachar = despachar;
        this.cancelar = cancelar;
        this.consultar = consultar;
    }

    @GetMapping("/requisicoes")
    public List<RequisicaoResumo> requisicoes() {
        return consultar.todas();
    }

    @GetMapping("/requisicoes/{id}")
    public RequisicaoResumo requisicao(@PathVariable String id) {
        return consultar.buscar(id);
    }

    @PostMapping("/requisicoes")
    public ResponseEntity<RequisicaoResumo> registrar(@Valid @RequestBody NovaRequisicaoRequest r) {
        RequisicaoResumo criada = registrar.executar(new NovaRequisicao(r.unidadeSolicitante(), r.prioridade(),
                r.justificativa(), r.itens().stream()
                .map(i -> new ItemPedido(i.hemocomponente(), i.tipoReceptor(), i.quantidade())).toList()));
        return ResponseEntity.created(URI.create("/requisicoes/" + criada.id())).body(criada);
    }

    /** História âncora: alocar bolsa a um item. 422 com "Tipo sanguíneo incompatível" etc. quando recusada. */
    @PostMapping("/requisicoes/{id}/itens/{itemId}/alocacoes")
    public RequisicaoResumo alocar(@PathVariable String id, @PathVariable String itemId,
                                   @Valid @RequestBody AlocacaoRequest r) {
        return alocar.executar(id, itemId, r.codigoBolsa());
    }

    @PostMapping("/requisicoes/{id}/atendimento")
    public RequisicaoResumo atender(@PathVariable String id) {
        return atender.executar(id);
    }

    @PostMapping("/requisicoes/{id}/despacho")
    public RequisicaoResumo despachar(@PathVariable String id) {
        return despachar.executar(id);
    }

    @PostMapping("/requisicoes/{id}/cancelamento")
    public RequisicaoResumo cancelar(@PathVariable String id, @RequestBody CancelamentoRequest r) {
        return cancelar.executar(id, r.motivo());
    }
}

package br.cesar.rotavital.captacao.apresentacao;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.cesar.rotavital.captacao.negocio.aplicacao.CadastrarDoador;
import br.cesar.rotavital.captacao.negocio.aplicacao.ConsultarDoadores;
import br.cesar.rotavital.captacao.negocio.aplicacao.RegistrarDoacao;
import br.cesar.rotavital.captacao.negocio.aplicacao.Resumos.DoacaoResumo;
import br.cesar.rotavital.captacao.negocio.aplicacao.Resumos.DoadorResumo;
import br.cesar.rotavital.captacao.negocio.aplicacao.Resumos.NovoDoador;
import br.cesar.rotavital.captacao.negocio.aplicacao.Resumos.TriagemResumo;
import br.cesar.rotavital.captacao.negocio.aplicacao.TriarDoador;
import br.cesar.rotavital.captacao.negocio.dominio.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/** Apresentação da Captação: H1 (triagem) e H2 (doação) da Parte 1, com as mesmas rotas. */
@RestController
public class DoadorController {

    /** Formato do JSON de entrada; a validação de negócio (CPF, idade...) fica no domínio. */
    public record NovoDoadorRequest(
            @NotBlank String nome,
            @NotBlank @Pattern(regexp = "\\d{11}", message = "deve conter 11 dígitos") String cpf,
            @NotNull @Past LocalDate dataNascimento,
            @NotNull Sexo sexo,
            @NotNull @Positive Double pesoKg,
            @NotBlank String tipoSanguineo,
            boolean consentimentoResponsavel) {

        NovoDoador paraPedido() {
            return new NovoDoador(nome, cpf, dataNascimento, sexo, pesoKg, tipoSanguineo, consentimentoResponsavel);
        }
    }

    private final CadastrarDoador cadastrar;
    private final ConsultarDoadores consultar;
    private final TriarDoador triar;
    private final RegistrarDoacao registrarDoacao;

    public DoadorController(CadastrarDoador cadastrar, ConsultarDoadores consultar, TriarDoador triar,
                            RegistrarDoacao registrarDoacao) {
        this.cadastrar = cadastrar;
        this.consultar = consultar;
        this.triar = triar;
        this.registrarDoacao = registrarDoacao;
    }

    @PostMapping("/doadores")
    public ResponseEntity<DoadorResumo> cadastrar(@Valid @RequestBody NovoDoadorRequest request) {
        DoadorResumo doador = cadastrar.executar(request.paraPedido());
        return ResponseEntity.created(URI.create("/doadores/" + doador.id())).body(doador);
    }

    @GetMapping("/doadores/{id}")
    public DoadorResumo buscar(@PathVariable Long id) {
        return consultar.buscar(id);
    }

    /** H1: sempre 200; o corpo diz se está apto e por quê. */
    @GetMapping("/doadores/{id}/triagem")
    public TriagemResumo triagem(@PathVariable Long id) {
        return triar.executar(id);
    }

    /** H2: registra a doação; 422 com os motivos se o doador estiver inapto. A bolsa entra no Estoque por evento. */
    @PostMapping("/doadores/{id}/doacoes")
    public ResponseEntity<DoacaoResumo> doar(@PathVariable Long id) {
        DoacaoResumo doacao = registrarDoacao.executar(id);
        return ResponseEntity.created(URI.create("/estoque/bolsas/" + doacao.codigoBolsa())).body(doacao);
    }

    /** Os 10 doadores cadastrados mais recentemente (usado pelo site). */
    @GetMapping("/api/doadores-recentes")
    public List<DoadorResumo> recentes() {
        return consultar.recentes(10);
    }
}

package br.cesar.rotavital.transporte.apresentacao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;
import br.cesar.rotavital.transporte.apresentacao.TradutorSensorFabricante.LeituraDoFabricante;
import br.cesar.rotavital.transporte.apresentacao.TradutorSensorFabricante.LeituraEmCelsius;
import br.cesar.rotavital.transporte.negocio.aplicacao.ConcluirEntrega;
import br.cesar.rotavital.transporte.negocio.aplicacao.ConsultarViagens;
import br.cesar.rotavital.transporte.negocio.aplicacao.RegistrarLeitura;
import br.cesar.rotavital.transporte.negocio.aplicacao.ViagemResumo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Apresentação do Transporte: viagens, telemetria do sensor e confirmação de chegada. */
@RestController
public class TransporteController {

    /** Corpo enviado pelo sensor do fabricante (°F). Sem horário, vale o horário de chegada. */
    public record LeituraSensorRequest(@NotNull Double temperaturaF, LocalDateTime capturadaEm) {
    }

    private final ConsultarViagens consultar;
    private final RegistrarLeitura registrarLeitura;
    private final ConcluirEntrega concluirEntrega;
    private final Clock clock;

    public TransporteController(ConsultarViagens consultar, RegistrarLeitura registrarLeitura,
                                ConcluirEntrega concluirEntrega, Clock clock) {
        this.consultar = consultar;
        this.registrarLeitura = registrarLeitura;
        this.concluirEntrega = concluirEntrega;
        this.clock = clock;
    }

    @GetMapping("/viagens")
    public List<ViagemResumo> viagens() {
        return consultar.todas();
    }

    @GetMapping("/viagens/{id}")
    public ViagemResumo viagem(@PathVariable String id) {
        return consultar.buscar(id).orElseThrow(() -> new NaoEncontradoException("Viagem " + id + " não encontrada"));
    }

    @PostMapping("/viagens/{id}/leituras")
    public ViagemResumo leitura(@PathVariable String id, @Valid @RequestBody LeituraSensorRequest r) {
        LocalDateTime capturadaEm = r.capturadaEm() != null ? r.capturadaEm() : LocalDateTime.now(clock);
        LeituraEmCelsius leitura = TradutorSensorFabricante.traduzir(
                new LeituraDoFabricante(r.temperaturaF(), capturadaEm));
        return registrarLeitura.executar(id, leitura.capturadaEm(), leitura.temperaturaC());
    }

    /** A caixa chegou ao destino: encerra a viagem e, por evento, a requisição. */
    @PostMapping("/viagens/{id}/entrega")
    public ViagemResumo entrega(@PathVariable String id) {
        return concluirEntrega.executar(id);
    }
}

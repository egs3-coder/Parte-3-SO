package br.cesar.rotavital.transporte.negocio.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;

/**
 * Raiz do agregado Viagem (contexto Transporte): leva a remessa até o hospital e prova que a caixa ficou na faixa.
 * "Entrega", aqui, é a chegada da caixa ao destino (o termo tem outro sentido na Requisição).
 */
public final class Viagem {

    public static final String ENCERRADA = "Viagem já encerrada";

    private final String id;
    private final String requisicaoId;
    private final String guia;
    private final FaixaTemperatura faixa;
    private final LocalDateTime iniciadaEm;
    private final List<LeituraTelemetria> leituras;
    private StatusViagem status;
    private LocalDateTime entregueEm;

    private Viagem(String id, String requisicaoId, String guia, FaixaTemperatura faixa, LocalDateTime iniciadaEm,
                   List<LeituraTelemetria> leituras, StatusViagem status, LocalDateTime entregueEm) {
        this.id = Objects.requireNonNull(id);
        this.requisicaoId = Objects.requireNonNull(requisicaoId);
        this.guia = Objects.requireNonNull(guia);
        this.faixa = Objects.requireNonNull(faixa);
        this.iniciadaEm = Objects.requireNonNull(iniciadaEm);
        this.leituras = new ArrayList<>(leituras);
        this.status = Objects.requireNonNull(status);
        this.entregueEm = entregueEm;
    }

    /** Uma viagem por requisição: o id leva o id da requisição (VG-REQ-001). */
    public static Viagem iniciar(String requisicaoId, String guia, FaixaTemperatura faixa, LocalDateTime agora) {
        return new Viagem("VG-" + requisicaoId, requisicaoId, guia, faixa, agora, List.of(), StatusViagem.EM_TRANSITO,
                null);
    }

    public static Viagem reconstituir(String id, String requisicaoId, String guia, FaixaTemperatura faixa,
                                      LocalDateTime iniciadaEm, List<LeituraTelemetria> leituras,
                                      StatusViagem status, LocalDateTime entregueEm) {
        return new Viagem(id, requisicaoId, guia, faixa, iniciadaEm, leituras, status, entregueEm);
    }

    /** Registra a leitura e diz se ela foi uma excursão de temperatura (fora da faixa). */
    public boolean registrarLeitura(LeituraTelemetria leitura) {
        exigirEmTransito();
        leituras.add(Objects.requireNonNull(leitura));
        return !faixa.contem(leitura.temperaturaC());
    }

    public void concluirEntrega(LocalDateTime agora) {
        exigirEmTransito();
        status = StatusViagem.ENTREGUE;
        entregueEm = agora;
    }

    /** Quantas leituras ficaram fora da faixa. */
    public long excursoes() {
        return leituras.stream().filter(l -> !faixa.contem(l.temperaturaC())).count();
    }

    public Optional<LeituraTelemetria> ultimaLeitura() {
        return leituras.isEmpty() ? Optional.empty() : Optional.of(leituras.get(leituras.size() - 1));
    }

    private void exigirEmTransito() {
        if (status != StatusViagem.EM_TRANSITO) {
            throw new RegraDeNegocioException(ENCERRADA);
        }
    }

    public String id() {
        return id;
    }

    public String requisicaoId() {
        return requisicaoId;
    }

    public String guia() {
        return guia;
    }

    public FaixaTemperatura faixa() {
        return faixa;
    }

    public LocalDateTime iniciadaEm() {
        return iniciadaEm;
    }

    public List<LeituraTelemetria> leituras() {
        return Collections.unmodifiableList(leituras);
    }

    public StatusViagem status() {
        return status;
    }

    public LocalDateTime entregueEm() {
        return entregueEm;
    }
}

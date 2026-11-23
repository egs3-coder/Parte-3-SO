package br.cesar.rotavital.transporte.negocio.aplicacao;

import java.time.LocalDateTime;

import br.cesar.rotavital.transporte.negocio.dominio.LeituraTelemetria;
import br.cesar.rotavital.transporte.negocio.dominio.Viagem;

/** O que o Transporte mostra de uma viagem. */
public record ViagemResumo(String id, String requisicaoId, String guia, double faixaMinimaC, double faixaMaximaC,
                           String status, LocalDateTime iniciadaEm, LocalDateTime entregueEm, int leituras,
                           long excursoes, Double ultimaTemperaturaC) {

    static ViagemResumo de(Viagem v) {
        return new ViagemResumo(v.id(), v.requisicaoId(), v.guia(), v.faixa().minimaC(), v.faixa().maximaC(),
                v.status().name(), v.iniciadaEm(), v.entregueEm(), v.leituras().size(), v.excursoes(),
                v.ultimaLeitura().map(LeituraTelemetria::temperaturaC).orElse(null));
    }
}

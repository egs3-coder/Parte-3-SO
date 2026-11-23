package br.cesar.rotavital.transporte.negocio.dominio;

import java.time.LocalDateTime;
import java.util.Objects;

/** Objeto de valor: temperatura (°C) e horário enviados pelo sensor da caixa, já traduzidos para o nosso modelo. */
public record LeituraTelemetria(LocalDateTime capturadaEm, double temperaturaC) {

    public LeituraTelemetria {
        Objects.requireNonNull(capturadaEm, "Leitura precisa do horário");
        if (Double.isNaN(temperaturaC) || temperaturaC < -80 || temperaturaC > 60) {
            throw new IllegalArgumentException("Temperatura fora do alcance do sensor: " + temperaturaC);
        }
    }
}

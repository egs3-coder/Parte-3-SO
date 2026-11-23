package br.cesar.rotavital.transporte.dados;

import java.time.LocalDateTime;

import br.cesar.rotavital.transporte.negocio.dominio.LeituraTelemetria;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Uma linha de transporte.leitura (coleção da viagem). */
@Embeddable
public class LeituraJpa {

    @Column(name = "capturada_em", nullable = false)
    private LocalDateTime capturadaEm;

    @Column(name = "temperatura_c", nullable = false)
    private double temperaturaC;

    protected LeituraJpa() {
    }

    static LeituraJpa de(LeituraTelemetria l) {
        LeituraJpa jpa = new LeituraJpa();
        jpa.capturadaEm = l.capturadaEm();
        jpa.temperaturaC = l.temperaturaC();
        return jpa;
    }

    LeituraTelemetria paraDominio() {
        return new LeituraTelemetria(capturadaEm, temperaturaC);
    }
}

package br.cesar.rotavital;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Rota Vital: monolito modular com um pacote por bounded context
 * (captacao, estoque, requisicao, expedicao, transporte, rede), cada um em três camadas.
 */
@SpringBootApplication
public class RotaVitalApplication {

    public static void main(String[] args) {
        SpringApplication.run(RotaVitalApplication.class, args);
    }

    /** Relógio injetável: as regras de data ficam testáveis sem depender do dia em que o teste roda. */
    @Bean
    Clock clock(@Value("${rotavital.fuso-horario:America/Recife}") String fusoHorario) {
        return Clock.system(ZoneId.of(fusoHorario));
    }
}

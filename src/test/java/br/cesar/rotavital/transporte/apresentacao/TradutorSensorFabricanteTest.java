package br.cesar.rotavital.transporte.apresentacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import br.cesar.rotavital.transporte.apresentacao.TradutorSensorFabricante.LeituraDoFabricante;

/** Camada anticorrupção: o domínio só recebe °C. */
class TradutorSensorFabricanteTest {

    @ParameterizedTest(name = "{0} °F = {1} °C")
    @CsvSource({"41, 5.0", "50, 10.0", "35.6, 2.0", "32, 0.0", "-4, -20.0"})
    @DisplayName("converte °F do fabricante para °C")
    void converte(double fahrenheit, double celsius) {
        LocalDateTime quando = LocalDateTime.of(2026, 11, 24, 20, 0);
        var leitura = TradutorSensorFabricante.traduzir(new LeituraDoFabricante(fahrenheit, quando));
        assertThat(leitura.temperaturaC()).isEqualTo(celsius);
        assertThat(leitura.capturadaEm()).isEqualTo(quando);
    }
}

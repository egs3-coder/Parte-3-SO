package br.cesar.rotavital.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class CodigoBolsaTest {

    @Test
    void normalizaParaMaiusculas() {
        assertThat(new CodigoBolsa(" b-100 ").valor()).isEqualTo("B-100");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "-B100", "B 100", "B/100"})
    void codigoInvalido(String valor) {
        assertThatThrownBy(() -> new CodigoBolsa(valor)).isInstanceOf(IllegalArgumentException.class);
    }
}

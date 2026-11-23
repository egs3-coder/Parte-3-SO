package br.cesar.rotavital.compartilhado.dominio;

import static br.cesar.rotavital.compartilhado.dominio.TipoSanguineo.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo.Antigeno;

/** Objeto de valor TipoSanguineo: grupo ABO + Rh e os antígenos de cada tipo. */
class TipoSanguineoTest {

    @Test
    @DisplayName("converte símbolo, inclusive minúsculo e com sinal de menos tipográfico")
    void converteSimbolo() {
        assertThat(deSimbolo("AB-")).isEqualTo(AB_NEGATIVO);
        assertThat(deSimbolo(" o+ ")).isEqualTo(O_POSITIVO);
        assertThat(deSimbolo("O−")).isEqualTo(O_NEGATIVO);
        assertThat(AB_NEGATIVO.simbolo()).isEqualTo("AB-");
    }

    @ParameterizedTest
    @ValueSource(strings = {"C+", "", "A", "AB"})
    void simboloInvalido(String simbolo) {
        assertThatThrownBy(() -> deSimbolo(simbolo)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("antígenos: O- não tem nenhum; AB+ tem A, B e D")
    void antigenos() {
        assertThat(O_NEGATIVO.antigenos()).isEmpty();
        assertThat(AB_POSITIVO.antigenos()).containsExactlyInAnyOrder(Antigeno.A, Antigeno.B, Antigeno.D);
        assertThat(A_POSITIVO.antigenosAbo()).containsExactly(Antigeno.A);
        assertThat(B_POSITIVO.rhPositivo()).isTrue();
        assertThat(B_NEGATIVO.rhPositivo()).isFalse();
    }

    @Test
    @DisplayName("imutável: alterar o conjunto devolvido não muda o tipo")
    void imutavel() {
        A_POSITIVO.antigenos().clear();
        assertThat(A_POSITIVO.antigenos()).containsExactlyInAnyOrder(Antigeno.A, Antigeno.D);
    }
}

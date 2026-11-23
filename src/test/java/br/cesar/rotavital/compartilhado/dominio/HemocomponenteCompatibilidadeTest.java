package br.cesar.rotavital.compartilhado.dominio;

import static br.cesar.rotavital.compartilhado.dominio.Hemocomponente.CONCENTRADO_HEMACIAS;
import static br.cesar.rotavital.compartilhado.dominio.Hemocomponente.PLASMA_FRESCO_CONGELADO;
import static br.cesar.rotavital.compartilhado.dominio.TipoSanguineo.deSimbolo;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Invariante I1: a compatibilidade depende do hemocomponente (devolutiva de 13/11 da Parte 2).
 * Hemácias levam antígenos; plasma leva anticorpos, então a regra se inverte e o Rh não conta.
 */
class HemocomponenteCompatibilidadeTest {

    @ParameterizedTest(name = "hemácias {0} → receptor {1}: {2}")
    @CsvSource({
            // bolsa, receptor, compatível
            "O-, O-, true", "O-, AB+, true", "O-, A+, true",
            "O+, O+, true", "O+, A+, true", "O+, O-, false", "O+, AB-, false",
            "A-, A+, true", "A-, AB-, true", "A-, B-, false", "A-, O-, false",
            "A+, A+, true", "A+, AB+, true", "A+, A-, false", "A+, B+, false",
            "B-, B+, true", "B-, AB-, true", "B-, A-, false",
            "B+, AB+, true", "B+, B-, false", "B+, A+, false",
            "AB-, AB+, true", "AB-, A-, false",
            "AB+, AB+, true", "AB+, AB-, false", "AB+, O+, false"
    })
    void hemacias(String bolsa, String receptor, boolean esperado) {
        assertThat(CONCENTRADO_HEMACIAS.compativel(deSimbolo(bolsa), deSimbolo(receptor))).isEqualTo(esperado);
    }

    @ParameterizedTest(name = "plasma {0} → receptor {1}: {2}")
    @CsvSource({
            // bolsa, receptor, compatível
            "AB-, A+, true", "AB+, O-, true", "AB-, B-, true",
            "A+, A-, true", "A-, O+, true", "A+, B+, false", "A-, AB+, false",
            "B+, B-, true", "B-, O-, true", "B+, A+, false",
            "O-, A+, false", "O+, O-, true", "O-, O+, true", "O-, AB-, false"
    })
    void plasma(String bolsa, String receptor, boolean esperado) {
        assertThat(PLASMA_FRESCO_CONGELADO.compativel(deSimbolo(bolsa), deSimbolo(receptor))).isEqualTo(esperado);
    }

    @Test
    @DisplayName("a mesma bolsa O- serve a um receptor A+ em hemácias, mas não em plasma")
    void regraSeInverte() {
        assertThat(CONCENTRADO_HEMACIAS.compativel(deSimbolo("O-"), deSimbolo("A+"))).isTrue();
        assertThat(PLASMA_FRESCO_CONGELADO.compativel(deSimbolo("O-"), deSimbolo("A+"))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(TipoSanguineo.class)
    @DisplayName("O- é doador universal de hemácias e AB é doador universal de plasma")
    void doadoresUniversais(TipoSanguineo receptor) {
        assertThat(CONCENTRADO_HEMACIAS.compativel(TipoSanguineo.O_NEGATIVO, receptor)).isTrue();
        assertThat(PLASMA_FRESCO_CONGELADO.compativel(TipoSanguineo.AB_NEGATIVO, receptor)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(TipoSanguineo.class)
    @DisplayName("todo tipo é compatível consigo mesmo nos dois hemocomponentes")
    void compativelConsigoMesmo(TipoSanguineo tipo) {
        assertThat(CONCENTRADO_HEMACIAS.compativel(tipo, tipo)).isTrue();
        assertThat(PLASMA_FRESCO_CONGELADO.compativel(tipo, tipo)).isTrue();
    }
}

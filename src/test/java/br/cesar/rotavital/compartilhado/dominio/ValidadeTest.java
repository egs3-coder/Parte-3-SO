package br.cesar.rotavital.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Objeto de valor Validade e a validade por hemocomponente (T2.1 da Parte 1). */
class ValidadeTest {

    private static final LocalDate COLETA = LocalDate.of(2026, 10, 1);

    @Test
    @DisplayName("hemácias valem 35 dias, até o fim do último dia")
    void hemacias35Dias() {
        Validade validade = Hemocomponente.CONCENTRADO_HEMACIAS.validadeParaColetaEm(COLETA);
        assertThat(validade.limite()).isEqualTo(LocalDateTime.of(2026, 11, 5, 23, 59));
    }

    @Test
    @DisplayName("plasma fresco congelado vale 365 dias")
    void plasma365Dias() {
        Validade validade = Hemocomponente.PLASMA_FRESCO_CONGELADO.validadeParaColetaEm(COLETA);
        assertThat(validade.limite().toLocalDate()).isEqualTo(LocalDate.of(2027, 10, 1));
    }

    @Test
    @DisplayName("no último minuto ainda vale; um minuto depois está vencida")
    void limiteInclusivo() {
        Validade validade = new Validade(LocalDateTime.of(2026, 11, 24, 21, 0));
        assertThat(validade.estaValidaEm(LocalDateTime.of(2026, 11, 24, 21, 0))).isTrue();
        assertThat(validade.vencidaEm(LocalDateTime.of(2026, 11, 24, 21, 1))).isTrue();
    }

    @Test
    @DisplayName("validade sem data é recusada no construtor")
    void semData() {
        assertThatThrownBy(() -> new Validade(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("igualdade por valor")
    void igualdade() {
        assertThat(Validade.ateOFimDoDia(COLETA)).isEqualTo(new Validade(COLETA.atTime(23, 59)));
    }
}

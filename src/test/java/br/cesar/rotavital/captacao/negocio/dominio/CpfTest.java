package br.cesar.rotavital.captacao.negocio.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** T2.5 da Parte 1, agora como objeto de valor: um CPF inválido não chega a existir. */
class CpfTest {

    @Test
    @DisplayName("CPF com dígitos verificadores corretos é criado")
    void cpfValido() {
        assertThat(new Cpf("52998224725").numero()).isEqualTo("52998224725");
    }

    @ParameterizedTest(name = "\"{0}\" é recusado no construtor")
    @NullSource
    @ValueSource(strings = {"52998224724", "11111111111", "123", "5299822472a", ""})
    void cpfInvalidoNaoExiste(String numero) {
        assertThatThrownBy(() -> new Cpf(numero)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("dois CPFs com o mesmo número são iguais (igualdade por valor)")
    void igualdadePorValor() {
        assertThat(new Cpf("52998224725")).isEqualTo(new Cpf("52998224725"));
    }

    @Test
    @DisplayName("valido() responde sem lançar exceção")
    void validoSemExcecao() {
        assertThat(Cpf.valido("52998224725")).isTrue();
        assertThat(Cpf.valido("52998224724")).isFalse();
    }
}

package br.cesar.rotavital.captacao.negocio.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;

/** H2: o agregado Doação só nasce de um doador aprovado na triagem. */
class DoacaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 24);

    private final Doador doador = Doador.reconstituir(7L, "Maria Sintética", new Cpf("52998224725"),
            LocalDate.of(1995, 3, 10), Sexo.FEMININO, 62.5, TipoSanguineo.O_NEGATIVO, false);

    @Test
    @DisplayName("doador apto: a doação leva o número (código da bolsa), o tipo do doador e a data")
    void doadorApto() {
        Doacao doacao = Doacao.registrar(doador, ResultadoTriagem.de(List.of()), new CodigoBolsa("RV-000001"), HOJE);

        assertThat(doacao.doadorId()).isEqualTo(7L);
        assertThat(doacao.numero()).isEqualTo(new CodigoBolsa("RV-000001"));
        assertThat(doacao.tipoSanguineo()).isEqualTo(TipoSanguineo.O_NEGATIVO);
        assertThat(doacao.dataColeta()).isEqualTo(HOJE);
    }

    @Test
    @DisplayName("doador inapto: recusa com todos os motivos")
    void doadorInapto() {
        var triagem = ResultadoTriagem.de(List.of("Peso mínimo para doação é 50 kg", "Idade máxima para doação é 69 anos"));

        assertThatThrownBy(() -> Doacao.registrar(doador, triagem, new CodigoBolsa("RV-000002"), HOJE))
                .isInstanceOf(DoadorInaptoException.class)
                .satisfies(e -> assertThat(((DoadorInaptoException) e).motivos()).hasSize(2));
    }

    @Test
    @DisplayName("doador novo não pode ter nascimento no futuro")
    void nascimentoNoFuturo() {
        assertThatThrownBy(() -> Doador.novo("Ana", new Cpf("52998224725"), HOJE.plusDays(1), Sexo.FEMININO, 60,
                TipoSanguineo.A_POSITIVO, false, HOJE)).isInstanceOf(IllegalArgumentException.class);
    }
}

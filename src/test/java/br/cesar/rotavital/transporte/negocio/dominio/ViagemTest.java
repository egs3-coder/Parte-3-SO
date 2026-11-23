package br.cesar.rotavital.transporte.negocio.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;

/** Agregado Viagem: telemetria, excursões de temperatura e entrega. */
class ViagemTest {

    private static final LocalDateTime SAIDA = LocalDateTime.of(2026, 11, 24, 20, 0);

    private Viagem viagem() {
        return Viagem.iniciar("REQ-001", "GR-REQ-001", FaixaTemperatura.HEMACIAS, SAIDA);
    }

    @Test
    @DisplayName("viagem nasce em trânsito com o id da requisição")
    void iniciada() {
        Viagem viagem = viagem();
        assertThat(viagem.id()).isEqualTo("VG-REQ-001");
        assertThat(viagem.status()).isEqualTo(StatusViagem.EM_TRANSITO);
        assertThat(viagem.excursoes()).isZero();
    }

    @Test
    @DisplayName("leitura dentro da faixa não é excursão; fora da faixa é")
    void excursao() {
        Viagem viagem = viagem();
        assertThat(viagem.registrarLeitura(new LeituraTelemetria(SAIDA.plusMinutes(1), 5.0))).isFalse();
        assertThat(viagem.registrarLeitura(new LeituraTelemetria(SAIDA.plusMinutes(2), 10.0))).isTrue();
        assertThat(viagem.excursoes()).isEqualTo(1);
        assertThat(viagem.ultimaLeitura()).get().extracting(LeituraTelemetria::temperaturaC).isEqualTo(10.0);
    }

    @Test
    @DisplayName("os limites da faixa (2 e 6 °C) ainda estão dentro")
    void limitesDaFaixa() {
        assertThat(FaixaTemperatura.HEMACIAS.contem(2.0)).isTrue();
        assertThat(FaixaTemperatura.HEMACIAS.contem(6.0)).isTrue();
        assertThat(FaixaTemperatura.HEMACIAS.contem(6.1)).isFalse();
    }

    @Test
    @DisplayName("viagem entregue não aceita leitura nem segunda entrega")
    void encerrada() {
        Viagem viagem = viagem();
        viagem.concluirEntrega(SAIDA.plusHours(1));
        assertThat(viagem.status()).isEqualTo(StatusViagem.ENTREGUE);
        assertThatThrownBy(() -> viagem.registrarLeitura(new LeituraTelemetria(SAIDA.plusHours(2), 4.0)))
                .isInstanceOf(RegraDeNegocioException.class).hasMessage(Viagem.ENCERRADA);
        assertThatThrownBy(() -> viagem.concluirEntrega(SAIDA.plusHours(2))).hasMessage(Viagem.ENCERRADA);
    }

    @Test
    @DisplayName("faixa com mínima maior que a máxima é recusada no construtor")
    void faixaInvalida() {
        assertThatThrownBy(() -> new FaixaTemperatura(6, 2)).isInstanceOf(IllegalArgumentException.class);
    }
}

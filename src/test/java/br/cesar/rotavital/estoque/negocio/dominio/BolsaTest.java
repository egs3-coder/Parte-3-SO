package br.cesar.rotavital.estoque.negocio.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;

/** Agregado Bolsa: o Estoque decide se a bolsa pode ser reservada, liberada ou descartada. */
class BolsaTest {

    private static final LocalDate COLETA = LocalDate.of(2026, 11, 1);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 24, 19, 0);

    private static Bolsa hemacias() {
        return Bolsa.registrar(new CodigoBolsa("B-100"), Hemocomponente.CONCENTRADO_HEMACIAS,
                TipoSanguineo.O_NEGATIVO, COLETA);
    }

    @Test
    @DisplayName("bolsa registrada entra disponível com a validade do hemocomponente")
    void registrada() {
        Bolsa bolsa = hemacias();
        assertThat(bolsa.status()).isEqualTo(StatusBolsa.DISPONIVEL);
        assertThat(bolsa.validade().limite()).isEqualTo(LocalDateTime.of(2026, 12, 6, 23, 59));
        assertThat(bolsa.utilizavelEm(AGORA)).isTrue();
    }

    @Test
    @DisplayName("reservar guarda para qual requisição")
    void reservar() {
        Bolsa bolsa = hemacias();
        bolsa.reservar("REQ-001", AGORA);
        assertThat(bolsa.status()).isEqualTo(StatusBolsa.RESERVADA);
        assertThat(bolsa.reservadaPara()).isEqualTo("REQ-001");
        assertThat(bolsa.utilizavelEm(AGORA)).isFalse();
    }

    @Test
    @DisplayName("bolsa reservada não é reservada de novo")
    void naoReservaDuasVezes() {
        Bolsa bolsa = hemacias();
        bolsa.reservar("REQ-001", AGORA);
        assertThatThrownBy(() -> bolsa.reservar("REQ-002", AGORA))
                .isInstanceOf(RegraDeNegocioException.class).hasMessage(Bolsa.JA_RESERVADA);
        assertThat(bolsa.reservadaPara()).isEqualTo("REQ-001");
    }

    @Test
    @DisplayName("bolsa vencida não pode ser reservada")
    void vencidaNaoReserva() {
        Bolsa bolsa = hemacias();
        assertThatThrownBy(() -> bolsa.reservar("REQ-001", LocalDateTime.of(2026, 12, 7, 0, 0)))
                .isInstanceOf(RegraDeNegocioException.class).hasMessage(Bolsa.VENCIDA);
        assertThat(bolsa.status()).isEqualTo(StatusBolsa.DISPONIVEL);
    }

    @Test
    @DisplayName("só a requisição que reservou pode liberar")
    void liberar() {
        Bolsa bolsa = hemacias();
        bolsa.reservar("REQ-001", AGORA);
        assertThatThrownBy(() -> bolsa.liberar("REQ-002")).hasMessage(Bolsa.RESERVA_DE_OUTRA);

        bolsa.liberar("REQ-001");
        assertThat(bolsa.status()).isEqualTo(StatusBolsa.DISPONIVEL);
        assertThat(bolsa.reservadaPara()).isNull();
    }

    @Test
    @DisplayName("bolsa reservada não é descartada; descartada não é reservada")
    void descarte() {
        Bolsa reservada = hemacias();
        reservada.reservar("REQ-001", AGORA);
        assertThatThrownBy(reservada::descartar).hasMessage(Bolsa.RESERVADA_NAO_DESCARTA);

        Bolsa descartada = hemacias();
        descartada.descartar();
        assertThatThrownBy(() -> descartada.reservar("REQ-001", AGORA)).hasMessage(Bolsa.DESCARTADA);
    }
}

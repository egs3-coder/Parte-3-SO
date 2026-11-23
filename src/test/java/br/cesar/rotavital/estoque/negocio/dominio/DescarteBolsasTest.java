package br.cesar.rotavital.estoque.negocio.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;

/** T4.2 da Parte 1: bolsas vencidas vão para descarte (agora sobre o agregado Bolsa). */
class DescarteBolsasTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 22);
    private static final LocalDateTime AGORA = HOJE.atTime(10, 0);

    private final DescarteBolsas descarte = new DescarteBolsas();

    /** Bolsa de hemácias que vence daqui a {@code diasParaVencer} dias (fim do dia). */
    private static Bolsa bolsa(String codigo, int diasParaVencer) {
        return Bolsa.registrar(new CodigoBolsa(codigo), Hemocomponente.CONCENTRADO_HEMACIAS,
                TipoSanguineo.A_POSITIVO, HOJE.plusDays(diasParaVencer).minusDays(35));
    }

    private List<String> codigos(List<Bolsa> bolsas) {
        return bolsas.stream().map(b -> b.codigo().valor()).toList();
    }

    @Test
    @DisplayName("bolsa que venceu ontem vai para descarte")
    void vencidaOntem() {
        assertThat(codigos(descarte.paraDescartar(List.of(bolsa("B-1", -1)), AGORA))).containsExactly("B-1");
    }

    @Test
    @DisplayName("bolsa que vence hoje ainda pode ser usada: não descarta")
    void venceHoje() {
        assertThat(descarte.paraDescartar(List.of(bolsa("B-1", 0)), AGORA)).isEmpty();
    }

    @Test
    @DisplayName("estoque misturado: só as vencidas, na ordem do estoque")
    void estoqueMisturado() {
        var estoque = List.of(bolsa("B-1", 10), bolsa("B-2", -3), bolsa("B-3", 0), bolsa("B-4", -40));
        assertThat(codigos(descarte.paraDescartar(estoque, AGORA))).containsExactly("B-2", "B-4");
    }

    @Test
    @DisplayName("bolsa reservada fica de fora do descarte automático")
    void reservadaFicaDeFora() {
        Bolsa reservada = bolsa("B-1", 1);
        reservada.reservar("REQ-001", AGORA);
        assertThat(descarte.paraDescartar(List.of(reservada), AGORA.plusDays(3))).isEmpty();
    }

    @Test
    @DisplayName("estoque vazio: nada a descartar")
    void estoqueVazio() {
        assertThat(descarte.paraDescartar(List.of(), AGORA)).isEmpty();
    }
}

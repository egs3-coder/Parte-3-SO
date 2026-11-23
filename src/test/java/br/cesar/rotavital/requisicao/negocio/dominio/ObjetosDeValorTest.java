package br.cesar.rotavital.requisicao.negocio.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Objetos de valor da Requisição: imutáveis, iguais por valor e validados no construtor. */
class ObjetosDeValorTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 24, 19, 0);

    @Test
    @DisplayName("prioridade define o prazo de entrega: rotina 24 h, urgência 4 h, emergência 1 h")
    void prazosDaPrioridade() {
        assertThat(Prioridade.ROTINA.entregaPrevista(AGORA)).isEqualTo(AGORA.plusHours(24));
        assertThat(Prioridade.URGENCIA.entregaPrevista(AGORA)).isEqualTo(LocalDateTime.of(2026, 11, 24, 23, 0));
        assertThat(Prioridade.EMERGENCIA.entregaPrevista(AGORA)).isEqualTo(AGORA.plusHours(1));
    }

    @Test
    @DisplayName("só a emergência exige justificativa clínica (I4)")
    void justificativaSoNaEmergencia() {
        assertThat(Prioridade.EMERGENCIA.exigeJustificativa()).isTrue();
        assertThat(Prioridade.URGENCIA.exigeJustificativa()).isFalse();
        assertThat(Prioridade.ROTINA.exigeJustificativa()).isFalse();
    }

    @Test
    void prioridadePeloNome() {
        assertThat(Prioridade.deNome("urgencia")).isEqualTo(Prioridade.URGENCIA);
        assertThatThrownBy(() -> Prioridade.deNome("ALTA")).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "quantidade {0} é recusada")
    @ValueSource(ints = {0, -1, 21})
    void quantidadeForaDoIntervalo(int valor) {
        assertThatThrownBy(() -> new Quantidade(valor)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void quantidadeValidaEIgualdadePorValor() {
        assertThat(new Quantidade(2)).isEqualTo(new Quantidade(2));
        assertThat(new Quantidade(20).valor()).isEqualTo(20);
    }

    @Test
    @DisplayName("justificativa: vazia não conta como informada; acima de 500 caracteres é recusada")
    void justificativa() {
        assertThat(JustificativaClinica.nenhuma().informada()).isFalse();
        assertThat(new JustificativaClinica("   ").informada()).isFalse();
        assertThat(new JustificativaClinica(" Politrauma, choque hemorrágico ").texto())
                .isEqualTo("Politrauma, choque hemorrágico");
        assertThatThrownBy(() -> new JustificativaClinica("x".repeat(501)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ids: REQ-001 e ITEM-1; formatos errados são recusados")
    void identidades() {
        assertThat(RequisicaoId.doNumero(1)).isEqualTo(new RequisicaoId("REQ-001"));
        assertThat(ItemId.daPosicao(2)).isEqualTo(new ItemId("ITEM-2"));
        assertThatThrownBy(() -> new RequisicaoId("REQ-1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ItemId("ITEM-0")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UnidadeSolicitanteId(" ")).isInstanceOf(IllegalArgumentException.class);
    }
}

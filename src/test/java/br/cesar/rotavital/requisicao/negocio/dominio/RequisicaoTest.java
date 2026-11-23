package br.cesar.rotavital.requisicao.negocio.dominio;

import static br.cesar.rotavital.compartilhado.dominio.Hemocomponente.CONCENTRADO_HEMACIAS;
import static br.cesar.rotavital.compartilhado.dominio.Hemocomponente.PLASMA_FRESCO_CONGELADO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.compartilhado.dominio.Validade;

/**
 * Agregado Requisição: a raiz é o único ponto de alteração e recusa toda operação que quebra uma invariante.
 * Mesmo cenário da história âncora da Parte 2: agora 24/11/2026 19:00, urgência (entrega prevista 23:00),
 * ITEM-1 = hemácias para receptor A+ (2 bolsas) e ITEM-2 = plasma para receptor A+ (2 bolsas).
 */
class RequisicaoTest {

    static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 24, 19, 0);
    static final Validade VALIDA = new Validade(LocalDateTime.of(2026, 12, 10, 0, 0));
    static final ItemId ITEM_1 = new ItemId("ITEM-1");
    static final ItemId ITEM_2 = new ItemId("ITEM-2");

    Requisicao requisicao;

    static BolsaReservada bolsa(String codigo, Hemocomponente hemocomponente, String tipo, Validade validade) {
        return new BolsaReservada(new CodigoBolsa(codigo), hemocomponente, TipoSanguineo.deSimbolo(tipo), validade);
    }

    static BolsaReservada hemacias(String codigo, String tipo) {
        return bolsa(codigo, CONCENTRADO_HEMACIAS, tipo, VALIDA);
    }

    static Requisicao registrar(Prioridade prioridade, JustificativaClinica justificativa, NovoItem... itens) {
        return Requisicao.registrar(new RequisicaoId("REQ-001"), new UnidadeSolicitanteId("HSA"), prioridade,
                justificativa, List.of(itens), AGORA);
    }

    static NovoItem item(Hemocomponente hemocomponente, String receptor, int quantidade) {
        return new NovoItem(hemocomponente, TipoSanguineo.deSimbolo(receptor), new Quantidade(quantidade));
    }

    @BeforeEach
    void requisicaoEmAtendimento() {
        requisicao = registrar(Prioridade.URGENCIA, JustificativaClinica.nenhuma(),
                item(CONCENTRADO_HEMACIAS, "A+", 2), item(PLASMA_FRESCO_CONGELADO, "A+", 2));
        requisicao.iniciarAtendimento();
    }

    /** Afirma que a operação foi recusada com a mensagem e que nada mudou na requisição. */
    void assertRecusaSemEfeito(Runnable operacao, String mensagem) {
        StatusRequisicao statusAntes = requisicao.status();
        List<CodigoBolsa> bolsasAntes = requisicao.bolsasAlocadas();

        assertThatThrownBy(operacao::run).isInstanceOf(RegraDeNegocioException.class).hasMessage(mensagem);

        assertThat(requisicao.status()).isEqualTo(statusAntes);
        assertThat(requisicao.bolsasAlocadas()).isEqualTo(bolsasAntes);
    }

    @Nested
    @DisplayName("registro (I3, I4)")
    class Registro {

        @Test
        @DisplayName("nasce REGISTRADA, com ITEM-1, ITEM-2... na ordem do pedido")
        void registrada() {
            Requisicao nova = registrar(Prioridade.ROTINA, null, item(CONCENTRADO_HEMACIAS, "O+", 1),
                    item(PLASMA_FRESCO_CONGELADO, "B-", 3));

            assertThat(nova.status()).isEqualTo(StatusRequisicao.REGISTRADA);
            assertThat(nova.itens()).extracting(ItemRequisicao::id).containsExactly(ITEM_1, ITEM_2);
            assertThat(nova.itens()).allSatisfy(i -> assertThat(i.quantidadeAlocada()).isZero());
        }

        @Test
        @DisplayName("I3: requisição sem itens é recusada")
        void semItens() {
            assertThatThrownBy(() -> registrar(Prioridade.ROTINA, null))
                    .isInstanceOf(RegraDeNegocioException.class).hasMessage(Requisicao.SEM_ITENS);
        }

        @Test
        @DisplayName("I4: emergência sem justificativa clínica é recusada; com justificativa, aceita")
        void emergencia() {
            assertThatThrownBy(() -> registrar(Prioridade.EMERGENCIA, JustificativaClinica.nenhuma(),
                    item(CONCENTRADO_HEMACIAS, "O-", 1)))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessage(Requisicao.EMERGENCIA_SEM_JUSTIFICATIVA);

            Requisicao comJustificativa = registrar(Prioridade.EMERGENCIA,
                    new JustificativaClinica("Politrauma com choque hemorrágico"), item(CONCENTRADO_HEMACIAS, "O-", 1));
            assertThat(comJustificativa.justificativa().informada()).isTrue();
        }

        @Test
        @DisplayName("a lista de itens não pode ser alterada por fora da raiz")
        void itensSoPelaRaiz() {
            assertThatThrownBy(() -> requisicao.itens().clear()).isInstanceOf(UnsupportedOperationException.class);
            ItemRequisicao item = requisicao.item(ITEM_1).orElseThrow();
            assertThatThrownBy(() -> item.alocacoes().clear()).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("alocar bolsa (I1, I2, I3)")
    class Alocar {

        @Test
        @DisplayName("hemácias O- válida para receptor A+: aceita, e o item passa a ter 1 bolsa")
        void aceita() {
            requisicao.alocar(ITEM_1, hemacias("B-100", "O-"), AGORA);

            ItemRequisicao item = requisicao.item(ITEM_1).orElseThrow();
            assertThat(item.quantidadeAlocada()).isEqualTo(1);
            assertThat(item.alocacoes().get(0).codigo()).isEqualTo(new CodigoBolsa("B-100"));
            assertThat(item.alocacoes().get(0).alocadaEm()).isEqualTo(AGORA);
        }

        @Test
        @DisplayName("I1: hemácias B+ para receptor A+ é recusada")
        void hemaciasIncompativel() {
            assertRecusaSemEfeito(() -> requisicao.alocar(ITEM_1, hemacias("B-200", "B+"), AGORA),
                    Requisicao.TIPO_INCOMPATIVEL);
        }

        @Test
        @DisplayName("I1: plasma AB- para receptor A+ é aceito; plasma O- é recusado (regra invertida)")
        void plasma() {
            requisicao.alocar(ITEM_2, bolsa("P-100", PLASMA_FRESCO_CONGELADO, "AB-", VALIDA), AGORA);
            assertThat(requisicao.item(ITEM_2).orElseThrow().quantidadeAlocada()).isEqualTo(1);

            assertRecusaSemEfeito(
                    () -> requisicao.alocar(ITEM_2, bolsa("P-200", PLASMA_FRESCO_CONGELADO, "O-", VALIDA), AGORA),
                    Requisicao.TIPO_INCOMPATIVEL);
        }

        @Test
        @DisplayName("I1: bolsa de hemocomponente diferente do item é recusada")
        void hemocomponenteDiferente() {
            assertRecusaSemEfeito(
                    () -> requisicao.alocar(ITEM_1, bolsa("P-300", PLASMA_FRESCO_CONGELADO, "AB-", VALIDA), AGORA),
                    Requisicao.HEMOCOMPONENTE_DIFERENTE);
        }

        @Test
        @DisplayName("I2: bolsa que vence às 21:00 não chega válida à entrega das 23:00 (urgência, 4 h)")
        void venceAntesDaEntrega() {
            Validade vence21h = new Validade(LocalDateTime.of(2026, 11, 24, 21, 0));
            assertRecusaSemEfeito(
                    () -> requisicao.alocar(ITEM_1, bolsa("B-300", CONCENTRADO_HEMACIAS, "O-", vence21h), AGORA),
                    Requisicao.VENCE_ANTES_DA_ENTREGA);
        }

        @Test
        @DisplayName("I2: bolsa que vence exatamente na entrega prevista ainda é aceita")
        void venceNaEntrega() {
            Validade vence23h = new Validade(LocalDateTime.of(2026, 11, 24, 23, 0));
            requisicao.alocar(ITEM_1, bolsa("B-301", CONCENTRADO_HEMACIAS, "O-", vence23h), AGORA);
            assertThat(requisicao.bolsasAlocadas()).containsExactly(new CodigoBolsa("B-301"));
        }

        @Test
        @DisplayName("I3: item com 2 bolsas não recebe a terceira")
        void itemCompleto() {
            requisicao.alocar(ITEM_1, hemacias("B-1", "O-"), AGORA);
            requisicao.alocar(ITEM_1, hemacias("B-2", "A+"), AGORA);

            assertRecusaSemEfeito(() -> requisicao.alocar(ITEM_1, hemacias("B-3", "O+"), AGORA),
                    Requisicao.ITEM_COMPLETO);
        }

        @Test
        @DisplayName("I3: a mesma bolsa não entra duas vezes na requisição")
        void bolsaRepetida() {
            requisicao.alocar(ITEM_1, hemacias("B-1", "O-"), AGORA);
            assertRecusaSemEfeito(() -> requisicao.alocar(ITEM_1, hemacias("B-1", "O-"), AGORA),
                    Requisicao.BOLSA_REPETIDA);
        }

        @Test
        @DisplayName("item que não existe na requisição é recusado")
        void itemInexistente() {
            assertThatThrownBy(() -> requisicao.alocar(new ItemId("ITEM-9"), hemacias("B-1", "O-"), AGORA))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageStartingWith(Requisicao.ITEM_INEXISTENTE);
        }

        @Test
        @DisplayName("I5: requisição só registrada (fora de atendimento) não aceita alocação")
        void foraDeAtendimento() {
            Requisicao registrada = registrar(Prioridade.ROTINA, null, item(CONCENTRADO_HEMACIAS, "A+", 1));
            assertThatThrownBy(() -> registrada.alocar(ITEM_1, hemacias("B-1", "O-"), AGORA))
                    .hasMessage(Requisicao.NAO_ESTA_EM_ATENDIMENTO);
            assertThat(registrada.bolsasAlocadas()).isEmpty();
        }

        @Test
        @DisplayName("desalocar devolve o item ao estado anterior (compensação)")
        void desalocar() {
            requisicao.alocar(ITEM_1, hemacias("B-1", "O-"), AGORA);
            requisicao.desalocar(ITEM_1, new CodigoBolsa("B-1"));
            assertThat(requisicao.bolsasAlocadas()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ciclo de vida (I5)")
    class CicloDeVida {

        void completarItens() {
            requisicao.alocar(ITEM_1, hemacias("B-1", "O-"), AGORA);
            requisicao.alocar(ITEM_1, hemacias("B-2", "A+"), AGORA);
            requisicao.alocar(ITEM_2, bolsa("P-1", PLASMA_FRESCO_CONGELADO, "AB-", VALIDA), AGORA);
            requisicao.alocar(ITEM_2, bolsa("P-2", PLASMA_FRESCO_CONGELADO, "A+", VALIDA), AGORA);
        }

        @Test
        @DisplayName("só fica ATENDIDA com todos os itens completos")
        void atendidaSoCompleta() {
            requisicao.alocar(ITEM_1, hemacias("B-1", "O-"), AGORA);
            assertRecusaSemEfeito(requisicao::marcarAtendida, Requisicao.ITENS_INCOMPLETOS);

            requisicao.alocar(ITEM_1, hemacias("B-2", "A+"), AGORA);
            requisicao.alocar(ITEM_2, bolsa("P-1", PLASMA_FRESCO_CONGELADO, "AB-", VALIDA), AGORA);
            requisicao.alocar(ITEM_2, bolsa("P-2", PLASMA_FRESCO_CONGELADO, "A+", VALIDA), AGORA);
            requisicao.marcarAtendida();
            assertThat(requisicao.status()).isEqualTo(StatusRequisicao.ATENDIDA);
        }

        @Test
        @DisplayName("ATENDIDA → (remessa) → EM_TRANSPORTE → ENTREGUE")
        void caminhoFeliz() {
            completarItens();
            requisicao.marcarAtendida();
            requisicao.registrarRemessa("GR-REQ-001");
            requisicao.iniciarTransporte("VG-REQ-001");
            assertThat(requisicao.status()).isEqualTo(StatusRequisicao.EM_TRANSPORTE);

            requisicao.confirmarEntrega();
            assertThat(requisicao.status()).isEqualTo(StatusRequisicao.ENTREGUE);
            assertThat(requisicao.guiaRemessa()).isEqualTo("GR-REQ-001");
            assertThat(requisicao.viagemId()).isEqualTo("VG-REQ-001");
        }

        @Test
        @DisplayName("não despacha sem remessa e não confirma entrega fora do transporte")
        void transicoesInvalidas() {
            completarItens();
            requisicao.marcarAtendida();
            assertRecusaSemEfeito(() -> requisicao.iniciarTransporte("VG-REQ-001"), Requisicao.DESPACHO_SEM_REMESSA);
            assertRecusaSemEfeito(requisicao::confirmarEntrega, Requisicao.ENTREGA_FORA_DO_TRANSPORTE);
        }

        @Test
        @DisplayName("ATENDIDA não aceita mais alocação")
        void atendidaNaoAloca() {
            completarItens();
            requisicao.marcarAtendida();
            assertRecusaSemEfeito(() -> requisicao.alocar(ITEM_1, hemacias("B-9", "O-"), AGORA),
                    Requisicao.NAO_ESTA_EM_ATENDIMENTO);
        }

        @Test
        @DisplayName("cancelar exige motivo e devolve as bolsas para o Estoque liberar")
        void cancelar() {
            requisicao.alocar(ITEM_1, hemacias("B-1", "O-"), AGORA);
            assertRecusaSemEfeito(() -> requisicao.cancelar(" "), Requisicao.CANCELAMENTO_SEM_MOTIVO);

            List<CodigoBolsa> liberar = requisicao.cancelar("Paciente transferido");
            assertThat(liberar).containsExactly(new CodigoBolsa("B-1"));
            assertThat(requisicao.status()).isEqualTo(StatusRequisicao.CANCELADA);
            assertThat(requisicao.motivoCancelamento()).isEqualTo("Paciente transferido");
        }

        @Test
        @DisplayName("em transporte não se cancela mais (ponto sem volta da saga)")
        void naoCancelaEmTransporte() {
            completarItens();
            requisicao.marcarAtendida();
            requisicao.registrarRemessa("GR-REQ-001");
            requisicao.iniciarTransporte("VG-REQ-001");
            assertRecusaSemEfeito(() -> requisicao.cancelar("Desistência"), Requisicao.NAO_CANCELAVEL);
        }
    }
}

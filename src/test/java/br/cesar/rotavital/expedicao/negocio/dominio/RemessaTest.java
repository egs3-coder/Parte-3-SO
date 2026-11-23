package br.cesar.rotavital.expedicao.negocio.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;

class RemessaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 24, 20, 0);

    @Test
    @DisplayName("remessa montada leva a guia da requisição e as bolsas")
    void montada() {
        Remessa remessa = Remessa.montar("REQ-001", "HSA", List.of(new CodigoBolsa("B-100")), AGORA);
        assertThat(remessa.guia()).isEqualTo(new GuiaRemessa("GR-REQ-001"));
        assertThat(remessa.status()).isEqualTo(StatusRemessa.MONTADA);
        assertThat(remessa.bolsas()).containsExactly(new CodigoBolsa("B-100"));
    }

    @Test
    @DisplayName("remessa sem bolsas ou com bolsa repetida é recusada")
    void invalidas() {
        assertThatThrownBy(() -> Remessa.montar("REQ-001", "HSA", List.of(), AGORA))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Remessa.montar("REQ-001", "HSA",
                List.of(new CodigoBolsa("B-1"), new CodigoBolsa("B-1")), AGORA))
                .hasMessage("Bolsa repetida na remessa");
    }

    @Test
    @DisplayName("a lista de bolsas não muda por fora do agregado")
    void listaImutavel() {
        List<CodigoBolsa> bolsas = new ArrayList<>(List.of(new CodigoBolsa("B-1")));
        Remessa remessa = Remessa.montar("REQ-001", "HSA", bolsas, AGORA);
        bolsas.add(new CodigoBolsa("B-2"));
        assertThat(remessa.bolsas()).hasSize(1);
        assertThatThrownBy(() -> remessa.bolsas().add(new CodigoBolsa("B-3")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void guiaInvalida() {
        assertThatThrownBy(() -> new GuiaRemessa("REQ-001")).isInstanceOf(IllegalArgumentException.class);
    }
}

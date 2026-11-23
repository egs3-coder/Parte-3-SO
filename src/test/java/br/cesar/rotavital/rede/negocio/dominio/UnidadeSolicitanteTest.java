package br.cesar.rotavital.rede.negocio.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UnidadeSolicitanteTest {

    @Test
    void cadastradaAtiva() {
        var unidade = UnidadeSolicitante.cadastrar(new CodigoUnidade("hsa"), "Hospital Sintético A",
                new Endereco("Recife", "pe"));
        assertThat(unidade.ativa()).isTrue();
        assertThat(unidade.codigo().valor()).isEqualTo("HSA");
        assertThat(unidade.endereco().uf()).isEqualTo("PE");
    }

    @Test
    void ufInvalida() {
        assertThatThrownBy(() -> new Endereco("Recife", "XX")).isInstanceOf(IllegalArgumentException.class);
    }
}

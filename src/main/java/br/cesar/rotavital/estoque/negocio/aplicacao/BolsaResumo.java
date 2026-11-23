package br.cesar.rotavital.estoque.negocio.aplicacao;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;

/**
 * Como o Estoque mostra uma bolsa para fora (apresentação e outros contextos). Os outros contextos usam este
 * resumo, nunca a classe {@link Bolsa} nem a tabela do Estoque.
 */
public record BolsaResumo(String codigo, Hemocomponente hemocomponente, String tipoSanguineo, LocalDate dataColeta,
                          LocalDateTime validade, String status, String reservadaPara) {

    static BolsaResumo de(Bolsa b) {
        return new BolsaResumo(b.codigo().valor(), b.hemocomponente(), b.tipoSanguineo().simbolo(), b.dataColeta(),
                b.validade().limite(), b.status().name(), b.reservadaPara());
    }
}

package br.cesar.rotavital.expedicao.negocio.aplicacao;

import java.time.LocalDateTime;
import java.util.List;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.expedicao.negocio.dominio.Remessa;

/** O que a Expedição mostra de uma remessa. */
public record RemessaResumo(String guia, String requisicaoId, String destino, List<String> bolsas,
                            LocalDateTime montadaEm, String status) {

    static RemessaResumo de(Remessa r) {
        return new RemessaResumo(r.guia().numero(), r.requisicaoId(), r.destino(),
                r.bolsas().stream().map(CodigoBolsa::valor).toList(), r.montadaEm(), r.status().name());
    }
}

package br.cesar.rotavital.expedicao.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.expedicao.negocio.dominio.GuiaRemessa;
import br.cesar.rotavital.expedicao.negocio.dominio.Remessa;
import br.cesar.rotavital.expedicao.negocio.dominio.RemessaRepository;

/**
 * Comando MontarRemessa da saga (Parte 2, D.3), chamado pela Requisição pelo adaptador {@code ExpedicaoClient}.
 * Idempotente: se a remessa da requisição já existe, devolve a mesma (o mesmo comando pode chegar duas vezes).
 */
@Service
public class MontarRemessa {

    private final RemessaRepository remessas;
    private final Clock clock;

    public MontarRemessa(RemessaRepository remessas, Clock clock) {
        this.remessas = remessas;
        this.clock = clock;
    }

    @Transactional
    public RemessaResumo executar(String requisicaoId, String destino, List<String> codigosBolsas) {
        return remessas.buscar(GuiaRemessa.daRequisicao(requisicaoId))
                .map(RemessaResumo::de)
                .orElseGet(() -> {
                    Remessa remessa = Remessa.montar(requisicaoId, destino,
                            codigosBolsas.stream().map(CodigoBolsa::new).toList(), LocalDateTime.now(clock));
                    remessas.salvar(remessa);
                    return RemessaResumo.de(remessa);
                });
    }
}

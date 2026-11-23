package br.cesar.rotavital.transporte.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.transporte.negocio.dominio.FaixaTemperatura;
import br.cesar.rotavital.transporte.negocio.dominio.Viagem;
import br.cesar.rotavital.transporte.negocio.dominio.ViagemRepository;

/** Comando AbrirViagem da saga (ponto sem volta), chamado pela Requisição pelo adaptador TransporteClient. */
@Service
public class AbrirViagem {

    private final ViagemRepository viagens;
    private final Clock clock;

    public AbrirViagem(ViagemRepository viagens, Clock clock) {
        this.viagens = viagens;
        this.clock = clock;
    }

    /** Idempotente: se a viagem da requisição já existe, devolve a mesma. */
    @Transactional
    public ViagemResumo executar(String requisicaoId, String guia) {
        return viagens.buscar("VG-" + requisicaoId)
                .map(ViagemResumo::de)
                .orElseGet(() -> {
                    Viagem viagem = Viagem.iniciar(requisicaoId, guia, FaixaTemperatura.HEMACIAS,
                            LocalDateTime.now(clock));
                    viagens.salvar(viagem);
                    return ViagemResumo.de(viagem);
                });
    }
}

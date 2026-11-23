package br.cesar.rotavital.transporte.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;
import br.cesar.rotavital.transporte.negocio.dominio.Viagem;
import br.cesar.rotavital.transporte.negocio.dominio.ViagemRepository;

/** Caso de uso: a caixa chegou. Encerra a viagem e publica {@link EntregaConcluida} (a Requisição consome). */
@Service
public class ConcluirEntrega {

    private final ViagemRepository viagens;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    public ConcluirEntrega(ViagemRepository viagens, ApplicationEventPublisher eventos, Clock clock) {
        this.viagens = viagens;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Transactional
    public ViagemResumo executar(String viagemId) {
        Viagem viagem = viagens.buscar(viagemId)
                .orElseThrow(() -> new NaoEncontradoException("Viagem " + viagemId + " não encontrada"));
        LocalDateTime agora = LocalDateTime.now(clock);
        viagem.concluirEntrega(agora);
        viagens.salvar(viagem);
        eventos.publishEvent(new EntregaConcluida(viagem.id(), viagem.requisicaoId(), agora));
        return ViagemResumo.de(viagem);
    }
}

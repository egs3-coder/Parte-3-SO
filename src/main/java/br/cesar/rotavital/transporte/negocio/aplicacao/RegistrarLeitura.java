package br.cesar.rotavital.transporte.negocio.aplicacao;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;
import br.cesar.rotavital.transporte.negocio.dominio.LeituraTelemetria;
import br.cesar.rotavital.transporte.negocio.dominio.Viagem;
import br.cesar.rotavital.transporte.negocio.dominio.ViagemRepository;

/** Caso de uso: registrar uma leitura de telemetria já em °C (a tradução do fabricante é feita antes, na borda). */
@Service
public class RegistrarLeitura {

    private final ViagemRepository viagens;

    public RegistrarLeitura(ViagemRepository viagens) {
        this.viagens = viagens;
    }

    @Transactional
    public ViagemResumo executar(String viagemId, LocalDateTime capturadaEm, double temperaturaC) {
        Viagem viagem = viagens.buscar(viagemId)
                .orElseThrow(() -> new NaoEncontradoException("Viagem " + viagemId + " não encontrada"));
        viagem.registrarLeitura(new LeituraTelemetria(capturadaEm, temperaturaC));
        viagens.salvar(viagem);
        return ViagemResumo.de(viagem);
    }
}

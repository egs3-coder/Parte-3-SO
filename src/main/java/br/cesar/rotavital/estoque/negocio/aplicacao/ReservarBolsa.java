package br.cesar.rotavital.estoque.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;
import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.BolsaRepository;

/**
 * Caso de uso: reservar e liberar bolsa para uma requisição. É a interface pública que o adaptador
 * {@code EstoqueClient} da Requisição chama (comandos ReservarBolsas e LiberarReserva da saga da Parte 2).
 */
@Service
public class ReservarBolsa {

    private final BolsaRepository bolsas;
    private final Clock clock;

    public ReservarBolsa(BolsaRepository bolsas, Clock clock) {
        this.bolsas = bolsas;
        this.clock = clock;
    }

    @Transactional
    public BolsaResumo reservar(String codigo, String requisicaoId) {
        Bolsa bolsa = travar(codigo);
        bolsa.reservar(requisicaoId, LocalDateTime.now(clock));
        bolsas.salvar(bolsa);
        return BolsaResumo.de(bolsa);
    }

    @Transactional
    public BolsaResumo liberar(String codigo, String requisicaoId) {
        Bolsa bolsa = travar(codigo);
        bolsa.liberar(requisicaoId);
        bolsas.salvar(bolsa);
        return BolsaResumo.de(bolsa);
    }

    private Bolsa travar(String codigo) {
        return bolsas.buscarParaAtualizar(new CodigoBolsa(codigo))
                .orElseThrow(() -> new NaoEncontradoException("Bolsa " + codigo + " não encontrada"));
    }
}

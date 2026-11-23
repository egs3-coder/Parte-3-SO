package br.cesar.rotavital.captacao.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.captacao.negocio.aplicacao.Resumos.TriagemResumo;
import br.cesar.rotavital.captacao.negocio.dominio.DoacaoRepository;
import br.cesar.rotavital.captacao.negocio.dominio.Doador;
import br.cesar.rotavital.captacao.negocio.dominio.DoadorRepository;
import br.cesar.rotavital.captacao.negocio.dominio.ResultadoTriagem;
import br.cesar.rotavital.captacao.negocio.dominio.TriagemDoador;
import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;

/** H1: o doador está apto? Devolve todos os motivos de uma vez. */
@Service
public class TriarDoador {

    private final DoadorRepository doadores;
    private final DoacaoRepository doacoes;
    private final Clock clock;
    private final TriagemDoador triagem = new TriagemDoador();

    public TriarDoador(DoadorRepository doadores, DoacaoRepository doacoes, Clock clock) {
        this.doadores = doadores;
        this.doacoes = doacoes;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TriagemResumo executar(Long doadorId) {
        return TriagemResumo.de(avaliar(buscar(doadorId)));
    }

    /** Usado pelo caso de uso RegistrarDoacao. */
    public Doador buscar(Long doadorId) {
        return doadores.buscar(doadorId)
                .orElseThrow(() -> new NaoEncontradoException("Doador " + doadorId + " não encontrado"));
    }

    public ResultadoTriagem avaliar(Doador doador) {
        return triagem.avaliar(doador.perfil(), doacoes.datasDeColetaDoDoador(doador.id()), LocalDate.now(clock));
    }
}

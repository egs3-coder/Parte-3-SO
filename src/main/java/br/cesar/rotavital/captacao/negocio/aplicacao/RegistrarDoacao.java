package br.cesar.rotavital.captacao.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.captacao.negocio.aplicacao.Resumos.DoacaoResumo;
import br.cesar.rotavital.captacao.negocio.dominio.Doacao;
import br.cesar.rotavital.captacao.negocio.dominio.DoacaoRepository;
import br.cesar.rotavital.captacao.negocio.dominio.Doador;

/**
 * H2: registrar doação. A regra "só doa quem a triagem aprova" está no agregado {@link Doacao};
 * aqui o caso de uso só orquestra e publica {@link DoacaoRegistrada}, que o Estoque consome na mesma transação.
 */
@Service
public class RegistrarDoacao {

    private final TriarDoador triagem;
    private final DoacaoRepository doacoes;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    public RegistrarDoacao(TriarDoador triagem, DoacaoRepository doacoes, ApplicationEventPublisher eventos,
                           Clock clock) {
        this.triagem = triagem;
        this.doacoes = doacoes;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Transactional
    public DoacaoResumo executar(Long doadorId) {
        Doador doador = triagem.buscar(doadorId);
        LocalDate hoje = LocalDate.now(clock);

        Doacao doacao = doacoes.salvar(
                Doacao.registrar(doador, triagem.avaliar(doador), doacoes.proximoNumero(), hoje));

        eventos.publishEvent(new DoacaoRegistrada(doacao.numero().valor(), doacao.tipoSanguineo(), hoje));
        return DoacaoResumo.de(doacao);
    }
}

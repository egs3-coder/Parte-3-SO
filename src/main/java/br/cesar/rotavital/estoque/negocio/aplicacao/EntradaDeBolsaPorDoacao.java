package br.cesar.rotavital.estoque.negocio.aplicacao;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.cesar.rotavital.captacao.negocio.aplicacao.DoacaoRegistrada;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.estoque.negocio.aplicacao.RegistrarBolsa.NovaBolsa;

/**
 * Consumidor do evento {@link DoacaoRegistrada} (publicado pela Captação): a bolsa de concentrado de hemácias
 * entra no estoque. Evento em memória, na mesma transação da doação: ou as duas coisas gravam, ou nenhuma.
 */
@Component
public class EntradaDeBolsaPorDoacao {

    private final RegistrarBolsa registrarBolsa;

    public EntradaDeBolsaPorDoacao(RegistrarBolsa registrarBolsa) {
        this.registrarBolsa = registrarBolsa;
    }

    @EventListener
    public void quandoDoacaoRegistrada(DoacaoRegistrada evento) {
        registrarBolsa.executar(new NovaBolsa(evento.codigoBolsa(), Hemocomponente.CONCENTRADO_HEMACIAS,
                evento.tipoSanguineo().simbolo(), evento.dataColeta()));
    }
}

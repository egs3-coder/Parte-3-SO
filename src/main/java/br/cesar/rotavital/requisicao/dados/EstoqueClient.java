package br.cesar.rotavital.requisicao.dados;

import org.springframework.stereotype.Component;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.compartilhado.dominio.Validade;
import br.cesar.rotavital.estoque.negocio.aplicacao.BolsaResumo;
import br.cesar.rotavital.estoque.negocio.aplicacao.ReservarBolsa;
import br.cesar.rotavital.requisicao.negocio.dominio.BolsaReservada;
import br.cesar.rotavital.requisicao.negocio.dominio.EstoqueGateway;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;

/**
 * Adaptador da porta {@link EstoqueGateway}: chama a interface pública do Estoque (camada de aplicação) e traduz a
 * resposta para a linguagem da Requisição. No monolito é uma chamada de método; com microsserviços vira o
 * cliente REST/mensagem do estoque-service, sem mudar o domínio.
 */
@Component
class EstoqueClient implements EstoqueGateway {

    private final ReservarBolsa reservarBolsa;

    EstoqueClient(ReservarBolsa reservarBolsa) {
        this.reservarBolsa = reservarBolsa;
    }

    @Override
    public BolsaReservada reservar(CodigoBolsa codigo, RequisicaoId requisicao) {
        BolsaResumo bolsa = reservarBolsa.reservar(codigo.valor(), requisicao.valor());
        return new BolsaReservada(new CodigoBolsa(bolsa.codigo()), bolsa.hemocomponente(),
                TipoSanguineo.deSimbolo(bolsa.tipoSanguineo()), new Validade(bolsa.validade()));
    }

    @Override
    public void liberar(CodigoBolsa codigo, RequisicaoId requisicao) {
        reservarBolsa.liberar(codigo.valor(), requisicao.valor());
    }
}

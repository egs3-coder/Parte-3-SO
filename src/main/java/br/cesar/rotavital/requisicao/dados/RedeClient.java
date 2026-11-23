package br.cesar.rotavital.requisicao.dados;

import org.springframework.stereotype.Component;

import br.cesar.rotavital.rede.negocio.aplicacao.ConsultarUnidades;
import br.cesar.rotavital.rede.negocio.aplicacao.UnidadeResumo;
import br.cesar.rotavital.requisicao.negocio.dominio.RedeGateway;
import br.cesar.rotavital.requisicao.negocio.dominio.UnidadeSolicitanteId;

/** Adaptador da porta {@link RedeGateway}: consulta o serviço aberto da Rede. */
@Component
class RedeClient implements RedeGateway {

    private final ConsultarUnidades unidades;

    RedeClient(ConsultarUnidades unidades) {
        this.unidades = unidades;
    }

    @Override
    public boolean unidadeAtiva(UnidadeSolicitanteId unidade) {
        try {
            return unidades.buscar(unidade.codigo()).map(UnidadeResumo::ativa).orElse(false);
        } catch (IllegalArgumentException codigoInvalido) {
            return false;
        }
    }
}

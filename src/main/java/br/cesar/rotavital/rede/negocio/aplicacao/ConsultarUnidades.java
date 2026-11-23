package br.cesar.rotavital.rede.negocio.aplicacao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.rede.negocio.dominio.CodigoUnidade;
import br.cesar.rotavital.rede.negocio.dominio.UnidadeRepository;

/** Interface pública de consulta da Rede (usada pela Requisição pelo adaptador RedeClient). */
@Service
@Transactional(readOnly = true)
public class ConsultarUnidades {

    private final UnidadeRepository unidades;

    public ConsultarUnidades(UnidadeRepository unidades) {
        this.unidades = unidades;
    }

    public List<UnidadeResumo> todas() {
        return unidades.todas().stream().map(UnidadeResumo::de).toList();
    }

    public Optional<UnidadeResumo> buscar(String codigo) {
        return unidades.buscar(new CodigoUnidade(codigo)).map(UnidadeResumo::de);
    }
}

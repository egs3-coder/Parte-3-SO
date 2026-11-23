package br.cesar.rotavital.expedicao.negocio.aplicacao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.expedicao.negocio.dominio.GuiaRemessa;
import br.cesar.rotavital.expedicao.negocio.dominio.RemessaRepository;

@Service
@Transactional(readOnly = true)
public class ConsultarRemessas {

    private final RemessaRepository remessas;

    public ConsultarRemessas(RemessaRepository remessas) {
        this.remessas = remessas;
    }

    public List<RemessaResumo> todas() {
        return remessas.todas().stream().map(RemessaResumo::de).toList();
    }

    public Optional<RemessaResumo> buscar(String numeroGuia) {
        return remessas.buscar(new GuiaRemessa(numeroGuia)).map(RemessaResumo::de);
    }
}

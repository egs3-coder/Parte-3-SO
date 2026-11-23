package br.cesar.rotavital.transporte.negocio.aplicacao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.transporte.negocio.dominio.ViagemRepository;

@Service
@Transactional(readOnly = true)
public class ConsultarViagens {

    private final ViagemRepository viagens;

    public ConsultarViagens(ViagemRepository viagens) {
        this.viagens = viagens;
    }

    public List<ViagemResumo> todas() {
        return viagens.todas().stream().map(ViagemResumo::de).toList();
    }

    public Optional<ViagemResumo> buscar(String id) {
        return viagens.buscar(id).map(ViagemResumo::de);
    }
}

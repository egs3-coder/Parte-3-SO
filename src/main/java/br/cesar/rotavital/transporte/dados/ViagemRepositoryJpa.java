package br.cesar.rotavital.transporte.dados;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.cesar.rotavital.transporte.negocio.dominio.Viagem;
import br.cesar.rotavital.transporte.negocio.dominio.ViagemRepository;

/** Adaptador da porta {@link ViagemRepository}. */
@Repository
class ViagemRepositoryJpa implements ViagemRepository {

    private final ViagemLinhas linhas;

    ViagemRepositoryJpa(ViagemLinhas linhas) {
        this.linhas = linhas;
    }

    @Override
    public void salvar(Viagem viagem) {
        linhas.save(ViagemJpa.de(viagem));
    }

    @Override
    public Optional<Viagem> buscar(String id) {
        return linhas.findById(id).map(ViagemJpa::paraDominio);
    }

    @Override
    public List<Viagem> todas() {
        return linhas.findAllByOrderByIniciadaEmDesc().stream().map(ViagemJpa::paraDominio).toList();
    }
}

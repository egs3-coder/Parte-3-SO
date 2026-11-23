package br.cesar.rotavital.expedicao.dados;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.cesar.rotavital.expedicao.negocio.dominio.GuiaRemessa;
import br.cesar.rotavital.expedicao.negocio.dominio.Remessa;
import br.cesar.rotavital.expedicao.negocio.dominio.RemessaRepository;

/** Adaptador da porta {@link RemessaRepository}. */
@Repository
class RemessaRepositoryJpa implements RemessaRepository {

    private final RemessaLinhas linhas;

    RemessaRepositoryJpa(RemessaLinhas linhas) {
        this.linhas = linhas;
    }

    @Override
    public void salvar(Remessa remessa) {
        linhas.save(RemessaJpa.de(remessa));
    }

    @Override
    public Optional<Remessa> buscar(GuiaRemessa guia) {
        return linhas.findById(guia.numero()).map(RemessaJpa::paraDominio);
    }

    @Override
    public List<Remessa> todas() {
        return linhas.findAllByOrderByMontadaEmDesc().stream().map(RemessaJpa::paraDominio).toList();
    }
}

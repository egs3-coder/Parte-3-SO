package br.cesar.rotavital.rede.dados;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.cesar.rotavital.rede.negocio.dominio.CodigoUnidade;
import br.cesar.rotavital.rede.negocio.dominio.UnidadeRepository;
import br.cesar.rotavital.rede.negocio.dominio.UnidadeSolicitante;

/** Adaptador da porta {@link UnidadeRepository}. */
@Repository
class UnidadeRepositoryJpa implements UnidadeRepository {

    private final UnidadeLinhas linhas;

    UnidadeRepositoryJpa(UnidadeLinhas linhas) {
        this.linhas = linhas;
    }

    @Override
    public void salvar(UnidadeSolicitante unidade) {
        linhas.save(UnidadeJpa.de(unidade));
    }

    @Override
    public Optional<UnidadeSolicitante> buscar(CodigoUnidade codigo) {
        return linhas.findById(codigo.valor()).map(UnidadeJpa::paraDominio);
    }

    @Override
    public List<UnidadeSolicitante> todas() {
        return linhas.findAllByOrderByNomeAsc().stream().map(UnidadeJpa::paraDominio).toList();
    }
}

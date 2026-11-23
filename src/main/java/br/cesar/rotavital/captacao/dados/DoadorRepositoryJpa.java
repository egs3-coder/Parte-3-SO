package br.cesar.rotavital.captacao.dados;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import br.cesar.rotavital.captacao.negocio.dominio.Cpf;
import br.cesar.rotavital.captacao.negocio.dominio.Doador;
import br.cesar.rotavital.captacao.negocio.dominio.DoadorRepository;

/** Adaptador: implementa a porta do domínio com Spring Data JPA. */
@Repository
class DoadorRepositoryJpa implements DoadorRepository {

    private final DoadorLinhas linhas;

    DoadorRepositoryJpa(DoadorLinhas linhas) {
        this.linhas = linhas;
    }

    @Override
    public Doador salvar(Doador doador) {
        return linhas.save(DoadorJpa.de(doador)).paraDominio();
    }

    @Override
    public Optional<Doador> buscar(Long id) {
        return linhas.findById(id).map(DoadorJpa::paraDominio);
    }

    @Override
    public boolean existeCpf(Cpf cpf) {
        return linhas.existsByCpf(cpf.numero());
    }

    @Override
    public List<Doador> recentes(int limite) {
        return linhas.findAll(PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "id")))
                .map(DoadorJpa::paraDominio)
                .getContent();
    }

    @Override
    public long contar() {
        return linhas.count();
    }
}

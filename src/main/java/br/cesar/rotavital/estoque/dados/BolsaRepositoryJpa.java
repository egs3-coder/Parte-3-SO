package br.cesar.rotavital.estoque.dados;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.BolsaRepository;
import br.cesar.rotavital.estoque.negocio.dominio.StatusBolsa;

/** Adaptador: implementa a porta {@link BolsaRepository} com Spring Data JPA. */
@Repository
class BolsaRepositoryJpa implements BolsaRepository {

    private final BolsaLinhas linhas;

    BolsaRepositoryJpa(BolsaLinhas linhas) {
        this.linhas = linhas;
    }

    @Override
    public void salvar(Bolsa bolsa) {
        BolsaJpa linha = linhas.findById(bolsa.codigo().valor()).orElseGet(BolsaJpa::nova);
        linha.copiar(bolsa);
        linhas.save(linha);
    }

    @Override
    public Optional<Bolsa> buscar(CodigoBolsa codigo) {
        return linhas.findById(codigo.valor()).map(BolsaJpa::paraDominio);
    }

    @Override
    public Optional<Bolsa> buscarParaAtualizar(CodigoBolsa codigo) {
        return linhas.travar(codigo.valor()).map(BolsaJpa::paraDominio);
    }

    @Override
    public boolean existe(CodigoBolsa codigo) {
        return linhas.existsById(codigo.valor());
    }

    @Override
    public List<Bolsa> emEstoque() {
        return linhas.findByStatusNotOrderByValidadeAscCodigoAsc(StatusBolsa.DESCARTADA).stream()
                .map(BolsaJpa::paraDominio)
                .toList();
    }
}

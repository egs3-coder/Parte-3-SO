package br.cesar.rotavital.captacao.dados;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import br.cesar.rotavital.captacao.negocio.dominio.Doacao;
import br.cesar.rotavital.captacao.negocio.dominio.DoacaoRepository;
import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;

/** Adaptador: implementa a porta de doações com Spring Data JPA. */
@Repository
class DoacaoRepositoryJpa implements DoacaoRepository {

    private final DoacaoLinhas linhas;

    DoacaoRepositoryJpa(DoacaoLinhas linhas) {
        this.linhas = linhas;
    }

    @Override
    public Doacao salvar(Doacao doacao) {
        return linhas.save(DoacaoJpa.de(doacao)).paraDominio();
    }

    @Override
    public List<LocalDate> datasDeColetaDoDoador(Long doadorId) {
        return linhas.datasDeColeta(doadorId);
    }

    @Override
    public CodigoBolsa proximoNumero() {
        return new CodigoBolsa(String.format("RV-%06d", linhas.proximoValorDaSequencia()));
    }

    @Override
    public long contar() {
        return linhas.count();
    }
}

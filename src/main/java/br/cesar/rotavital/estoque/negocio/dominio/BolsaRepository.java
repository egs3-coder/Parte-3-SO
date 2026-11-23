package br.cesar.rotavital.estoque.negocio.dominio;

import java.util.List;
import java.util.Optional;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;

/** Porta do domínio: a camada de dados implementa. */
public interface BolsaRepository {

    void salvar(Bolsa bolsa);

    Optional<Bolsa> buscar(CodigoBolsa codigo);

    /** Mesma busca, mas trava a bolsa até o fim da transação: duas requisições não reservam a mesma bolsa. */
    Optional<Bolsa> buscarParaAtualizar(CodigoBolsa codigo);

    boolean existe(CodigoBolsa codigo);

    /** Todas as bolsas que não foram descartadas, da que vence antes para a que vence depois. */
    List<Bolsa> emEstoque();
}

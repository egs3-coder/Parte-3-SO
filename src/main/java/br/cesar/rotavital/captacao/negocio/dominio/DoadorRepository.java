package br.cesar.rotavital.captacao.negocio.dominio;

import java.util.List;
import java.util.Optional;

/** Porta do domínio: a camada de dados implementa (inversão de dependência). */
public interface DoadorRepository {

    Doador salvar(Doador doador);

    Optional<Doador> buscar(Long id);

    boolean existeCpf(Cpf cpf);

    List<Doador> recentes(int limite);

    long contar();
}

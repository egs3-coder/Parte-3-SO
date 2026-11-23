package br.cesar.rotavital.requisicao.negocio.dominio;

import java.util.List;
import java.util.Optional;

/** Porta do domínio: a camada de dados implementa (RequisicaoRepositoryJdbc). */
public interface RequisicaoRepository {

    RequisicaoId proximoId();

    void salvar(Requisicao requisicao);

    Optional<Requisicao> buscar(RequisicaoId id);

    /** Mesma busca, mas trava a requisição até o fim da transação (duas atendentes não alocam ao mesmo tempo). */
    Optional<Requisicao> buscarParaAtualizar(RequisicaoId id);

    List<Requisicao> todas();
}

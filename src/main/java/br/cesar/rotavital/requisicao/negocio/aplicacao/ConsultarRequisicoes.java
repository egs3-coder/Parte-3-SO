package br.cesar.rotavital.requisicao.negocio.aplicacao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoRepository;

/** Consultas da Requisição. */
@Service
@Transactional(readOnly = true)
public class ConsultarRequisicoes {

    private final RequisicaoRepository requisicoes;

    public ConsultarRequisicoes(RequisicaoRepository requisicoes) {
        this.requisicoes = requisicoes;
    }

    public List<RequisicaoResumo> todas() {
        return requisicoes.todas().stream().map(RequisicaoResumo::de).toList();
    }

    public RequisicaoResumo buscar(String id) {
        return RequisicaoResumo.de(carregar(requisicoes, new RequisicaoId(id)));
    }

    static Requisicao carregar(RequisicaoRepository requisicoes, RequisicaoId id) {
        return requisicoes.buscar(id).orElseThrow(() -> naoEncontrada(id));
    }

    /** Para os casos de uso que alteram a requisição: carrega com trava. */
    static Requisicao carregarParaAtualizar(RequisicaoRepository requisicoes, RequisicaoId id) {
        return requisicoes.buscarParaAtualizar(id).orElseThrow(() -> naoEncontrada(id));
    }

    private static NaoEncontradoException naoEncontrada(RequisicaoId id) {
        return new NaoEncontradoException("Requisição " + id + " não encontrada");
    }
}

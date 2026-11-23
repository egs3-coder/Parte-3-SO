package br.cesar.rotavital.rede.negocio.aplicacao;

import br.cesar.rotavital.rede.negocio.dominio.UnidadeSolicitante;

/** Linguagem publicada da Rede (serviço aberto, OHS, na Parte 2): o que os outros contextos veem da unidade. */
public record UnidadeResumo(String codigo, String nome, String cidade, String uf, boolean ativa) {

    static UnidadeResumo de(UnidadeSolicitante u) {
        return new UnidadeResumo(u.codigo().valor(), u.nome(), u.endereco().cidade(), u.endereco().uf(), u.ativa());
    }
}

package br.cesar.rotavital.rede.negocio.dominio;

import java.util.List;
import java.util.Optional;

/** Porta do domínio da Rede. */
public interface UnidadeRepository {

    void salvar(UnidadeSolicitante unidade);

    Optional<UnidadeSolicitante> buscar(CodigoUnidade codigo);

    List<UnidadeSolicitante> todas();
}

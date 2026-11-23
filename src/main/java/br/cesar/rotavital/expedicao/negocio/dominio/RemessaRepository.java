package br.cesar.rotavital.expedicao.negocio.dominio;

import java.util.List;
import java.util.Optional;

/** Porta do domínio da Expedição. */
public interface RemessaRepository {

    void salvar(Remessa remessa);

    Optional<Remessa> buscar(GuiaRemessa guia);

    List<Remessa> todas();
}

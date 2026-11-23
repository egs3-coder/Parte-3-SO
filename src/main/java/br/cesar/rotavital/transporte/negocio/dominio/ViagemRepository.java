package br.cesar.rotavital.transporte.negocio.dominio;

import java.util.List;
import java.util.Optional;

/** Porta do domínio do Transporte. */
public interface ViagemRepository {

    void salvar(Viagem viagem);

    Optional<Viagem> buscar(String id);

    List<Viagem> todas();
}

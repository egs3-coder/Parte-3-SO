package br.cesar.rotavital.transporte.dados;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA para transporte.viagem. */
public interface ViagemLinhas extends JpaRepository<ViagemJpa, String> {

    List<ViagemJpa> findAllByOrderByIniciadaEmDesc();
}

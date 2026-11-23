package br.cesar.rotavital.expedicao.dados;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA para expedicao.remessa. */
public interface RemessaLinhas extends JpaRepository<RemessaJpa, String> {

    List<RemessaJpa> findAllByOrderByMontadaEmDesc();
}

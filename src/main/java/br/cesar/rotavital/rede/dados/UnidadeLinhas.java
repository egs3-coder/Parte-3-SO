package br.cesar.rotavital.rede.dados;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA para rede.unidade. */
public interface UnidadeLinhas extends JpaRepository<UnidadeJpa, String> {

    List<UnidadeJpa> findAllByOrderByNomeAsc();
}

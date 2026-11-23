package br.cesar.rotavital.captacao.dados;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA para captacao.doador. */
public interface DoadorLinhas extends JpaRepository<DoadorJpa, Long> {
    boolean existsByCpf(String cpf);
}

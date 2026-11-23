package br.cesar.rotavital.estoque.dados;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.cesar.rotavital.estoque.negocio.dominio.StatusBolsa;
import jakarta.persistence.LockModeType;

/** Spring Data JPA para estoque.bolsa. */
public interface BolsaLinhas extends JpaRepository<BolsaJpa, String> {

    List<BolsaJpa> findByStatusNotOrderByValidadeAscCodigoAsc(StatusBolsa status);

    /** SELECT … FOR UPDATE: a linha fica travada até o commit. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BolsaJpa b where b.codigo = :codigo")
    Optional<BolsaJpa> travar(@Param("codigo") String codigo);
}

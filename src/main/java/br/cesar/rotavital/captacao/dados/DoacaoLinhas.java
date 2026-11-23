package br.cesar.rotavital.captacao.dados;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA para captacao.doacao. */
public interface DoacaoLinhas extends JpaRepository<DoacaoJpa, Long> {

    @Query("select d.dataColeta from DoacaoJpa d where d.doadorId = :doadorId")
    List<LocalDate> datasDeColeta(@Param("doadorId") Long doadorId);

    @Query(value = "select nextval('captacao.doacao_numero_seq')", nativeQuery = true)
    long proximoValorDaSequencia();
}

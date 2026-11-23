package br.cesar.rotavital.captacao.dados;

import java.time.LocalDate;

import br.cesar.rotavital.captacao.negocio.dominio.Doacao;
import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Linha da tabela captacao.doacao. */
@Entity
@Table(schema = "captacao", name = "doacao")
public class DoacaoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doador_id", nullable = false)
    private Long doadorId;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sanguineo", nullable = false, length = 12)
    private TipoSanguineo tipoSanguineo;

    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;

    protected DoacaoJpa() {
    }

    static DoacaoJpa de(Doacao d) {
        DoacaoJpa jpa = new DoacaoJpa();
        jpa.id = d.id();
        jpa.doadorId = d.doadorId();
        jpa.numero = d.numero().valor();
        jpa.tipoSanguineo = d.tipoSanguineo();
        jpa.dataColeta = d.dataColeta();
        return jpa;
    }

    Doacao paraDominio() {
        return Doacao.reconstituir(id, doadorId, new CodigoBolsa(numero), tipoSanguineo, dataColeta);
    }
}

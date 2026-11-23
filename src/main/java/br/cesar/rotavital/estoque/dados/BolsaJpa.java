package br.cesar.rotavital.estoque.dados;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.compartilhado.dominio.Validade;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.StatusBolsa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Linha da tabela estoque.bolsa. Nenhum outro contexto lê esta tabela. */
@Entity
@Table(schema = "estoque", name = "bolsa")
public class BolsaJpa {

    @Id
    @Column(length = 30)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Hemocomponente hemocomponente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sanguineo", nullable = false, length = 12)
    private TipoSanguineo tipoSanguineo;

    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;

    @Column(nullable = false)
    private LocalDateTime validade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusBolsa status;

    @Column(name = "reservada_para", length = 30)
    private String reservadaPara;

    /** Controle otimista de concorrência, além da trava pessimista na reserva. */
    @Version
    private Long versao;

    protected BolsaJpa() {
    }

    static BolsaJpa nova() {
        return new BolsaJpa();
    }

    void copiar(Bolsa b) {
        codigo = b.codigo().valor();
        hemocomponente = b.hemocomponente();
        tipoSanguineo = b.tipoSanguineo();
        dataColeta = b.dataColeta();
        validade = b.validade().limite();
        status = b.status();
        reservadaPara = b.reservadaPara();
    }

    Bolsa paraDominio() {
        return Bolsa.reconstituir(new CodigoBolsa(codigo), hemocomponente, tipoSanguineo, dataColeta,
                new Validade(validade), status, reservadaPara);
    }
}

package br.cesar.rotavital.expedicao.dados;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.expedicao.negocio.dominio.GuiaRemessa;
import br.cesar.rotavital.expedicao.negocio.dominio.Remessa;
import br.cesar.rotavital.expedicao.negocio.dominio.StatusRemessa;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/** Linhas das tabelas expedicao.remessa e expedicao.remessa_bolsa. */
@Entity
@Table(schema = "expedicao", name = "remessa")
public class RemessaJpa {

    @Id
    @Column(length = 50)
    private String guia;

    @Column(name = "requisicao_id", nullable = false, unique = true, length = 30)
    private String requisicaoId;

    @Column(nullable = false, length = 20)
    private String destino;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "expedicao", name = "remessa_bolsa", joinColumns = @JoinColumn(name = "guia"))
    @OrderColumn(name = "posicao")
    @Column(name = "codigo_bolsa", nullable = false, length = 30)
    private List<String> bolsas = new ArrayList<>();

    @Column(name = "montada_em", nullable = false)
    private LocalDateTime montadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusRemessa status;

    protected RemessaJpa() {
    }

    static RemessaJpa de(Remessa r) {
        RemessaJpa jpa = new RemessaJpa();
        jpa.guia = r.guia().numero();
        jpa.requisicaoId = r.requisicaoId();
        jpa.destino = r.destino();
        jpa.bolsas = new ArrayList<>(r.bolsas().stream().map(CodigoBolsa::valor).toList());
        jpa.montadaEm = r.montadaEm();
        jpa.status = r.status();
        return jpa;
    }

    Remessa paraDominio() {
        return Remessa.reconstituir(new GuiaRemessa(guia), requisicaoId, destino,
                bolsas.stream().map(CodigoBolsa::new).toList(), montadaEm, status);
    }
}

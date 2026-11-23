package br.cesar.rotavital.transporte.dados;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.cesar.rotavital.transporte.negocio.dominio.FaixaTemperatura;
import br.cesar.rotavital.transporte.negocio.dominio.StatusViagem;
import br.cesar.rotavital.transporte.negocio.dominio.Viagem;
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

/** Linhas das tabelas transporte.viagem e transporte.leitura. */
@Entity
@Table(schema = "transporte", name = "viagem")
public class ViagemJpa {

    @Id
    @Column(length = 40)
    private String id;

    @Column(name = "requisicao_id", nullable = false, unique = true, length = 30)
    private String requisicaoId;

    @Column(nullable = false, length = 50)
    private String guia;

    @Column(name = "faixa_minima_c", nullable = false)
    private double faixaMinimaC;

    @Column(name = "faixa_maxima_c", nullable = false)
    private double faixaMaximaC;

    @Column(name = "iniciada_em", nullable = false)
    private LocalDateTime iniciadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusViagem status;

    @Column(name = "entregue_em")
    private LocalDateTime entregueEm;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "transporte", name = "leitura", joinColumns = @JoinColumn(name = "viagem_id"))
    @OrderColumn(name = "posicao")
    private List<LeituraJpa> leituras = new ArrayList<>();

    protected ViagemJpa() {
    }

    static ViagemJpa de(Viagem v) {
        ViagemJpa jpa = new ViagemJpa();
        jpa.id = v.id();
        jpa.requisicaoId = v.requisicaoId();
        jpa.guia = v.guia();
        jpa.faixaMinimaC = v.faixa().minimaC();
        jpa.faixaMaximaC = v.faixa().maximaC();
        jpa.iniciadaEm = v.iniciadaEm();
        jpa.status = v.status();
        jpa.entregueEm = v.entregueEm();
        jpa.leituras = new ArrayList<>(v.leituras().stream().map(LeituraJpa::de).toList());
        return jpa;
    }

    Viagem paraDominio() {
        return Viagem.reconstituir(id, requisicaoId, guia, new FaixaTemperatura(faixaMinimaC, faixaMaximaC),
                iniciadaEm, leituras.stream().map(LeituraJpa::paraDominio).toList(), status, entregueEm);
    }
}

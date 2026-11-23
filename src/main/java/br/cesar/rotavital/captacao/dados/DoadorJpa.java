package br.cesar.rotavital.captacao.dados;

import java.time.LocalDate;

import br.cesar.rotavital.captacao.negocio.dominio.Cpf;
import br.cesar.rotavital.captacao.negocio.dominio.Doador;
import br.cesar.rotavital.captacao.negocio.dominio.Sexo;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Linha da tabela captacao.doador. Só a camada de dados conhece esta classe. */
@Entity
@Table(schema = "captacao", name = "doador")
public class DoadorJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sexo sexo;

    @Column(name = "peso_kg", nullable = false)
    private double pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sanguineo", nullable = false, length = 12)
    private TipoSanguineo tipoSanguineo;

    @Column(name = "consentimento_responsavel", nullable = false)
    private boolean consentimentoResponsavel;

    protected DoadorJpa() {
    }

    static DoadorJpa de(Doador d) {
        DoadorJpa jpa = new DoadorJpa();
        jpa.id = d.id();
        jpa.nome = d.nome();
        jpa.cpf = d.cpf().numero();
        jpa.dataNascimento = d.dataNascimento();
        jpa.sexo = d.sexo();
        jpa.pesoKg = d.pesoKg();
        jpa.tipoSanguineo = d.tipoSanguineo();
        jpa.consentimentoResponsavel = d.consentimentoResponsavel();
        return jpa;
    }

    Doador paraDominio() {
        return Doador.reconstituir(id, nome, new Cpf(cpf), dataNascimento, sexo, pesoKg, tipoSanguineo,
                consentimentoResponsavel);
    }
}

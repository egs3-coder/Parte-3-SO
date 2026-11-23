package br.cesar.rotavital.captacao.negocio.dominio;

import java.time.LocalDate;
import java.util.Objects;

import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;

/** Raiz do agregado Doador (contexto Captação). Sem anotação de JPA: a persistência fica na camada de dados. */
public final class Doador {

    private final Long id;
    private final String nome;
    private final Cpf cpf;
    private final LocalDate dataNascimento;
    private final Sexo sexo;
    private final double pesoKg;
    private final TipoSanguineo tipoSanguineo;
    private final boolean consentimentoResponsavel;

    private Doador(Long id, String nome, Cpf cpf, LocalDate dataNascimento, Sexo sexo, double pesoKg,
                   TipoSanguineo tipoSanguineo, boolean consentimentoResponsavel) {
        if (nome == null || nome.isBlank() || nome.length() > 120) {
            throw new IllegalArgumentException("Nome do doador é obrigatório (até 120 caracteres)");
        }
        if (pesoKg <= 0) {
            throw new IllegalArgumentException("Peso deve ser positivo");
        }
        this.id = id;
        this.nome = nome.trim();
        this.cpf = Objects.requireNonNull(cpf, "CPF é obrigatório");
        this.dataNascimento = Objects.requireNonNull(dataNascimento, "Data de nascimento é obrigatória");
        this.sexo = Objects.requireNonNull(sexo, "Sexo é obrigatório");
        this.pesoKg = pesoKg;
        this.tipoSanguineo = Objects.requireNonNull(tipoSanguineo, "Tipo sanguíneo é obrigatório");
        this.consentimentoResponsavel = consentimentoResponsavel;
    }

    /** Doador novo, ainda sem id: quem dá o id é o repositório ao salvar. */
    public static Doador novo(String nome, Cpf cpf, LocalDate dataNascimento, Sexo sexo, double pesoKg,
                              TipoSanguineo tipoSanguineo, boolean consentimentoResponsavel, LocalDate hoje) {
        if (dataNascimento != null && dataNascimento.isAfter(hoje)) {
            throw new IllegalArgumentException("Data de nascimento no futuro");
        }
        return new Doador(null, nome, cpf, dataNascimento, sexo, pesoKg, tipoSanguineo, consentimentoResponsavel);
    }

    /** Reconstrói um doador já gravado (usado pela camada de dados). */
    public static Doador reconstituir(Long id, String nome, Cpf cpf, LocalDate dataNascimento, Sexo sexo,
                                      double pesoKg, TipoSanguineo tipoSanguineo, boolean consentimentoResponsavel) {
        return new Doador(Objects.requireNonNull(id), nome, cpf, dataNascimento, sexo, pesoKg, tipoSanguineo,
                consentimentoResponsavel);
    }

    /** O que a triagem precisa saber sobre este doador. */
    public PerfilDoador perfil() {
        return new PerfilDoador(dataNascimento, sexo, pesoKg, consentimentoResponsavel);
    }

    public Long id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public Cpf cpf() {
        return cpf;
    }

    public LocalDate dataNascimento() {
        return dataNascimento;
    }

    public Sexo sexo() {
        return sexo;
    }

    public double pesoKg() {
        return pesoKg;
    }

    public TipoSanguineo tipoSanguineo() {
        return tipoSanguineo;
    }

    public boolean consentimentoResponsavel() {
        return consentimentoResponsavel;
    }
}

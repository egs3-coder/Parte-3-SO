package br.cesar.rotavital.rede.dados;

import br.cesar.rotavital.rede.negocio.dominio.CodigoUnidade;
import br.cesar.rotavital.rede.negocio.dominio.Endereco;
import br.cesar.rotavital.rede.negocio.dominio.UnidadeSolicitante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Linha da tabela rede.unidade. */
@Entity
@Table(schema = "rede", name = "unidade")
public class UnidadeJpa {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 80)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(nullable = false)
    private boolean ativa;

    protected UnidadeJpa() {
    }

    static UnidadeJpa de(UnidadeSolicitante u) {
        UnidadeJpa jpa = new UnidadeJpa();
        jpa.codigo = u.codigo().valor();
        jpa.nome = u.nome();
        jpa.cidade = u.endereco().cidade();
        jpa.uf = u.endereco().uf();
        jpa.ativa = u.ativa();
        return jpa;
    }

    UnidadeSolicitante paraDominio() {
        return new UnidadeSolicitante(new CodigoUnidade(codigo), nome, new Endereco(cidade, uf), ativa);
    }
}

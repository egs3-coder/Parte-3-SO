package br.cesar.rotavital.rede.negocio.dominio;

import java.util.Objects;

/** Raiz do agregado: hospital ou unidade que pode fazer requisições ao hemocentro. */
public final class UnidadeSolicitante {

    private final CodigoUnidade codigo;
    private final String nome;
    private final Endereco endereco;
    private boolean ativa;

    public UnidadeSolicitante(CodigoUnidade codigo, String nome, Endereco endereco, boolean ativa) {
        if (nome == null || nome.isBlank() || nome.length() > 120) {
            throw new IllegalArgumentException("Nome da unidade é obrigatório (até 120 caracteres)");
        }
        this.codigo = Objects.requireNonNull(codigo);
        this.nome = nome.trim();
        this.endereco = Objects.requireNonNull(endereco);
        this.ativa = ativa;
    }

    public static UnidadeSolicitante cadastrar(CodigoUnidade codigo, String nome, Endereco endereco) {
        return new UnidadeSolicitante(codigo, nome, endereco, true);
    }

    public void desativar() {
        ativa = false;
    }

    public CodigoUnidade codigo() {
        return codigo;
    }

    public String nome() {
        return nome;
    }

    public Endereco endereco() {
        return endereco;
    }

    public boolean ativa() {
        return ativa;
    }
}

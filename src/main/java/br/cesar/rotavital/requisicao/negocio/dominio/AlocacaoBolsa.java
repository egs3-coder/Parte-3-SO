package br.cesar.rotavital.requisicao.negocio.dominio;

import java.time.LocalDateTime;
import java.util.Objects;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.compartilhado.dominio.Validade;

/**
 * Entidade do agregado: uma bolsa alocada a um item. A identidade é o código da bolsa.
 * Não tem método que mude estado: quem cria e remove alocações é a raiz {@link Requisicao}.
 */
public final class AlocacaoBolsa {

    private final CodigoBolsa codigo;
    private final Hemocomponente hemocomponente;
    private final TipoSanguineo tipoSanguineo;
    private final Validade validade;
    private final LocalDateTime alocadaEm;

    private AlocacaoBolsa(CodigoBolsa codigo, Hemocomponente hemocomponente, TipoSanguineo tipoSanguineo,
                          Validade validade, LocalDateTime alocadaEm) {
        this.codigo = Objects.requireNonNull(codigo);
        this.hemocomponente = Objects.requireNonNull(hemocomponente);
        this.tipoSanguineo = Objects.requireNonNull(tipoSanguineo);
        this.validade = Objects.requireNonNull(validade);
        this.alocadaEm = Objects.requireNonNull(alocadaEm);
    }

    static AlocacaoBolsa de(BolsaReservada bolsa, LocalDateTime agora) {
        return new AlocacaoBolsa(bolsa.codigo(), bolsa.hemocomponente(), bolsa.tipoSanguineo(), bolsa.validade(),
                agora);
    }

    /** Reconstrói uma alocação gravada (camada de dados). */
    public static AlocacaoBolsa reconstituir(CodigoBolsa codigo, Hemocomponente hemocomponente,
                                             TipoSanguineo tipoSanguineo, Validade validade,
                                             LocalDateTime alocadaEm) {
        return new AlocacaoBolsa(codigo, hemocomponente, tipoSanguineo, validade, alocadaEm);
    }

    public CodigoBolsa codigo() {
        return codigo;
    }

    public Hemocomponente hemocomponente() {
        return hemocomponente;
    }

    public TipoSanguineo tipoSanguineo() {
        return tipoSanguineo;
    }

    public Validade validade() {
        return validade;
    }

    public LocalDateTime alocadaEm() {
        return alocadaEm;
    }
}

package br.cesar.rotavital.captacao.negocio.dominio;

import java.time.LocalDate;
import java.util.Objects;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;

/**
 * Raiz do agregado Doação. O número da doação é também o código da bolsa que entra no Estoque.
 * Regra H2 da Parte 1: só existe doação de quem a triagem aprovou.
 */
public final class Doacao {

    private final Long id;
    private final Long doadorId;
    private final CodigoBolsa numero;
    private final TipoSanguineo tipoSanguineo;
    private final LocalDate dataColeta;

    private Doacao(Long id, Long doadorId, CodigoBolsa numero, TipoSanguineo tipoSanguineo, LocalDate dataColeta) {
        this.id = id;
        this.doadorId = Objects.requireNonNull(doadorId, "Doação precisa do doador");
        this.numero = Objects.requireNonNull(numero, "Doação precisa de número");
        this.tipoSanguineo = Objects.requireNonNull(tipoSanguineo, "Doação precisa do tipo sanguíneo");
        this.dataColeta = Objects.requireNonNull(dataColeta, "Doação precisa da data de coleta");
    }

    /** Registra a coleta; recusa com todos os motivos se a triagem não aprovou o doador. */
    public static Doacao registrar(Doador doador, ResultadoTriagem triagem, CodigoBolsa numero, LocalDate hoje) {
        if (!triagem.apto()) {
            throw new DoadorInaptoException(triagem.motivos());
        }
        return new Doacao(null, doador.id(), numero, doador.tipoSanguineo(), hoje);
    }

    public static Doacao reconstituir(Long id, Long doadorId, CodigoBolsa numero, TipoSanguineo tipoSanguineo,
                                      LocalDate dataColeta) {
        return new Doacao(Objects.requireNonNull(id), doadorId, numero, tipoSanguineo, dataColeta);
    }

    public Long id() {
        return id;
    }

    public Long doadorId() {
        return doadorId;
    }

    public CodigoBolsa numero() {
        return numero;
    }

    public TipoSanguineo tipoSanguineo() {
        return tipoSanguineo;
    }

    public LocalDate dataColeta() {
        return dataColeta;
    }
}

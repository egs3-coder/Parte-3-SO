package br.cesar.rotavital.estoque.negocio.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.compartilhado.dominio.Validade;

/**
 * Raiz do agregado Bolsa (contexto Estoque): unidade de hemocomponente com código, tipo, validade e status.
 * O Estoque é o dono da verdade da bolsa: só ele decide se ela pode ser reservada, liberada ou descartada.
 */
public final class Bolsa {

    public static final String JA_RESERVADA = "Bolsa já reservada";
    public static final String VENCIDA = "Bolsa vencida não pode ser reservada";
    public static final String DESCARTADA = "Bolsa descartada não pode ser reservada";
    public static final String RESERVA_DE_OUTRA = "Bolsa não está reservada para esta requisição";
    public static final String RESERVADA_NAO_DESCARTA = "Bolsa reservada não pode ser descartada";

    private final CodigoBolsa codigo;
    private final Hemocomponente hemocomponente;
    private final TipoSanguineo tipoSanguineo;
    private final LocalDate dataColeta;
    private final Validade validade;
    private StatusBolsa status;
    private String reservadaPara;

    private Bolsa(CodigoBolsa codigo, Hemocomponente hemocomponente, TipoSanguineo tipoSanguineo,
                  LocalDate dataColeta, Validade validade, StatusBolsa status, String reservadaPara) {
        this.codigo = Objects.requireNonNull(codigo, "Bolsa precisa de código");
        this.hemocomponente = Objects.requireNonNull(hemocomponente, "Bolsa precisa de hemocomponente");
        this.tipoSanguineo = Objects.requireNonNull(tipoSanguineo, "Bolsa precisa de tipo sanguíneo");
        this.dataColeta = Objects.requireNonNull(dataColeta, "Bolsa precisa de data de coleta");
        this.validade = Objects.requireNonNull(validade, "Bolsa precisa de validade");
        this.status = Objects.requireNonNull(status);
        this.reservadaPara = reservadaPara;
    }

    /** Bolsa recém-coletada: entra disponível, com a validade do seu hemocomponente. */
    public static Bolsa registrar(CodigoBolsa codigo, Hemocomponente hemocomponente, TipoSanguineo tipo,
                                  LocalDate dataColeta) {
        Objects.requireNonNull(hemocomponente, "Bolsa precisa de hemocomponente");
        Objects.requireNonNull(dataColeta, "Bolsa precisa de data de coleta");
        return new Bolsa(codigo, hemocomponente, tipo, dataColeta, hemocomponente.validadeParaColetaEm(dataColeta),
                StatusBolsa.DISPONIVEL, null);
    }

    /** Reconstrói uma bolsa gravada (camada de dados). */
    public static Bolsa reconstituir(CodigoBolsa codigo, Hemocomponente hemocomponente, TipoSanguineo tipo,
                                     LocalDate dataColeta, Validade validade, StatusBolsa status,
                                     String reservadaPara) {
        return new Bolsa(codigo, hemocomponente, tipo, dataColeta, validade, status, reservadaPara);
    }

    /** Bloqueia a bolsa para uma requisição. Só bolsa disponível e dentro da validade. */
    public void reservar(String requisicaoId, LocalDateTime agora) {
        if (requisicaoId == null || requisicaoId.isBlank()) {
            throw new IllegalArgumentException("Reserva precisa da requisição");
        }
        if (status == StatusBolsa.RESERVADA) {
            throw new RegraDeNegocioException(JA_RESERVADA);
        }
        if (status == StatusBolsa.DESCARTADA) {
            throw new RegraDeNegocioException(DESCARTADA);
        }
        if (validade.vencidaEm(agora)) {
            throw new RegraDeNegocioException(VENCIDA);
        }
        status = StatusBolsa.RESERVADA;
        reservadaPara = requisicaoId;
    }

    /** Desfaz a reserva (compensação). Só a requisição que reservou pode liberar. */
    public void liberar(String requisicaoId) {
        if (status != StatusBolsa.RESERVADA || !reservadaPara.equals(requisicaoId)) {
            throw new RegraDeNegocioException(RESERVA_DE_OUTRA);
        }
        status = StatusBolsa.DISPONIVEL;
        reservadaPara = null;
    }

    public void descartar() {
        if (status == StatusBolsa.RESERVADA) {
            throw new RegraDeNegocioException(RESERVADA_NAO_DESCARTA);
        }
        status = StatusBolsa.DESCARTADA;
    }

    /** Pode ser oferecida a uma requisição agora? */
    public boolean utilizavelEm(LocalDateTime agora) {
        return status == StatusBolsa.DISPONIVEL && validade.estaValidaEm(agora);
    }

    public boolean vencidaEm(LocalDateTime agora) {
        return validade.vencidaEm(agora);
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

    public LocalDate dataColeta() {
        return dataColeta;
    }

    public Validade validade() {
        return validade;
    }

    public StatusBolsa status() {
        return status;
    }

    public String reservadaPara() {
        return reservadaPara;
    }
}

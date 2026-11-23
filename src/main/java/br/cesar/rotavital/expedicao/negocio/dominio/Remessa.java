package br.cesar.rotavital.expedicao.negocio.dominio;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;

/**
 * Raiz do agregado Remessa (contexto Expedição): as bolsas separadas e acondicionadas na caixa térmica, com a guia.
 * Na Expedição, "bolsa" é só um volume a conferir: importa o código, não o tipo nem a validade (glossário da Parte 2).
 */
public final class Remessa {

    private final GuiaRemessa guia;
    private final String requisicaoId;
    private final String destino;
    private final List<CodigoBolsa> bolsas;
    private final LocalDateTime montadaEm;
    private StatusRemessa status;

    private Remessa(GuiaRemessa guia, String requisicaoId, String destino, List<CodigoBolsa> bolsas,
                    LocalDateTime montadaEm, StatusRemessa status) {
        this.guia = Objects.requireNonNull(guia);
        this.requisicaoId = Objects.requireNonNull(requisicaoId);
        this.destino = Objects.requireNonNull(destino);
        this.bolsas = List.copyOf(bolsas);
        this.montadaEm = Objects.requireNonNull(montadaEm);
        this.status = Objects.requireNonNull(status);
    }

    public static Remessa montar(String requisicaoId, String destino, List<CodigoBolsa> bolsas, LocalDateTime agora) {
        if (bolsas == null || bolsas.isEmpty()) {
            throw new RegraDeNegocioException("Remessa precisa de ao menos uma bolsa");
        }
        if (new HashSet<>(bolsas).size() != bolsas.size()) {
            throw new RegraDeNegocioException("Bolsa repetida na remessa");
        }
        return new Remessa(GuiaRemessa.daRequisicao(requisicaoId), requisicaoId, destino, bolsas, agora,
                StatusRemessa.MONTADA);
    }

    public static Remessa reconstituir(GuiaRemessa guia, String requisicaoId, String destino,
                                       List<CodigoBolsa> bolsas, LocalDateTime montadaEm, StatusRemessa status) {
        return new Remessa(guia, requisicaoId, destino, bolsas, montadaEm, status);
    }

    public void cancelar() {
        status = StatusRemessa.CANCELADA;
    }

    public GuiaRemessa guia() {
        return guia;
    }

    public String requisicaoId() {
        return requisicaoId;
    }

    public String destino() {
        return destino;
    }

    public List<CodigoBolsa> bolsas() {
        return bolsas;
    }

    public LocalDateTime montadaEm() {
        return montadaEm;
    }

    public StatusRemessa status() {
        return status;
    }
}

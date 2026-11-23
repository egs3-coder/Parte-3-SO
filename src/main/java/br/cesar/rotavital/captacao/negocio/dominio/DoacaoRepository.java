package br.cesar.rotavital.captacao.negocio.dominio;

import java.time.LocalDate;
import java.util.List;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;

/** Porta do domínio para as doações. */
public interface DoacaoRepository {

    Doacao salvar(Doacao doacao);

    /** Histórico usado pela triagem (intervalo e limite anual). */
    List<LocalDate> datasDeColetaDoDoador(Long doadorId);

    /** Próximo número de doação (RV-000001, RV-000002...). */
    CodigoBolsa proximoNumero();

    long contar();
}

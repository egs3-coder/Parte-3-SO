package br.cesar.rotavital.compartilhado.dominio;

import java.time.LocalDate;

/**
 * Objeto de valor: o que a bolsa contém. Nesta fase, concentrado de hemácias e plasma fresco congelado.
 *
 * <p>A compatibilidade (invariante I1 da Parte 2) depende do hemocomponente:
 * <ul>
 *   <li><b>Hemácias</b> levam os antígenos: todo antígeno A, B e D da bolsa precisa existir no receptor
 *       (O− serve a todos; receptor Rh− só recebe Rh−).</li>
 *   <li><b>Plasma</b> leva os anticorpos, então a regra se inverte: os antígenos A e B do receptor precisam existir
 *       na bolsa (plasma AB serve a todos; plasma O só a receptor O) e o Rh não conta.</li>
 * </ul>
 */
public enum Hemocomponente {

    CONCENTRADO_HEMACIAS("Concentrado de hemácias", 35) {
        @Override
        public boolean compativel(TipoSanguineo doador, TipoSanguineo receptor) {
            return true; // TODO verde: antígenos da bolsa precisam existir no receptor
        }
    },

    PLASMA_FRESCO_CONGELADO("Plasma fresco congelado", 365) {
        @Override
        public boolean compativel(TipoSanguineo doador, TipoSanguineo receptor) {
            return true; // TODO verde: regra invertida, Rh não conta
        }
    };

    private final String descricao;
    private final int diasDeValidade;

    Hemocomponente(String descricao, int diasDeValidade) {
        this.descricao = descricao;
        this.diasDeValidade = diasDeValidade;
    }

    /** A bolsa do tipo {@code doador} pode ser transfundida num receptor do tipo {@code receptor}? */
    public abstract boolean compativel(TipoSanguineo doador, TipoSanguineo receptor);

    /** Hemácias em CPDA-1: 35 dias. Plasma fresco congelado: 12 meses. Vale até o fim do último dia. */
    public Validade validadeParaColetaEm(LocalDate dataColeta) {
        return Validade.ateOFimDoDia(dataColeta.plusDays(diasDeValidade));
    }

    public int diasDeValidade() {
        return diasDeValidade;
    }

    public String descricao() {
        return descricao;
    }

    public static Hemocomponente deNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Hemocomponente é obrigatório");
        }
        try {
            return valueOf(nome.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Hemocomponente inválido: " + nome);
        }
    }
}

package br.cesar.rotavital.estoque.negocio.dominio;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;

/**
 * T4.1 da Parte 1 - Estoque crítico de concentrado de hemácias.
 * Os tipos O têm mínimo maior: O− é o doador universal (emergências sem tipagem) e O+ é o tipo mais comum.
 */
public class AlertaEstoque {

    static final int MINIMO_TIPO_O = 5;
    static final int MINIMO_DEMAIS_TIPOS = 3;

    public static int minimo(TipoSanguineo tipo) {
        return (tipo == TipoSanguineo.O_NEGATIVO || tipo == TipoSanguineo.O_POSITIVO)
                ? MINIMO_TIPO_O
                : MINIMO_DEMAIS_TIPOS;
    }

    /** Tipos abaixo do mínimo. Tipo ausente no mapa conta como zero bolsas. */
    public List<TipoSanguineo> tiposCriticos(Map<TipoSanguineo, Integer> estoque) {
        return Arrays.stream(TipoSanguineo.values())
                .filter(tipo -> estoque.getOrDefault(tipo, 0) < minimo(tipo))
                .toList();
    }
}

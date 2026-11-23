package br.cesar.rotavital.estoque.negocio.dominio;

import java.time.LocalDateTime;
import java.util.List;

/**
 * T4.2 da Parte 1 - Descarte de bolsas vencidas. Serviço de domínio: escolhe, entre as bolsas disponíveis,
 * as que já venceram (a bolsa vale até o fim do último dia, inclusive). Reservadas ficam de fora.
 */
public class DescarteBolsas {

    /** Bolsas a descartar, na mesma ordem em que aparecem no estoque. */
    public List<Bolsa> paraDescartar(List<Bolsa> estoque, LocalDateTime agora) {
        return estoque.stream()
                .filter(bolsa -> bolsa.status() == StatusBolsa.DISPONIVEL)
                .filter(bolsa -> bolsa.vencidaEm(agora))
                .toList();
    }
}

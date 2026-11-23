package br.cesar.rotavital.transporte.apresentacao;

import java.time.LocalDateTime;

/**
 * Camada anticorrupção (mapa de contextos da Parte 2: Sensor → Transporte).
 * O fabricante manda a temperatura em °F; o Rota Vital só trabalha em °C. A tradução fica aqui, na borda de
 * entrada do contexto: o domínio nunca vê o modelo do fabricante.
 */
public final class TradutorSensorFabricante {

    /** Payload como o sensor do fabricante envia. */
    public record LeituraDoFabricante(double temperaturaF, LocalDateTime capturadaEm) {
    }

    /** Leitura no nosso modelo. */
    public record LeituraEmCelsius(LocalDateTime capturadaEm, double temperaturaC) {
    }

    private TradutorSensorFabricante() {
    }

    public static LeituraEmCelsius traduzir(LeituraDoFabricante leitura) {
        double celsius = (leitura.temperaturaF() - 32.0) * 5.0 / 9.0;
        // uma casa decimal, como o display da caixa
        return new LeituraEmCelsius(leitura.capturadaEm(), Math.round(celsius * 10.0) / 10.0);
    }
}

package br.cesar.rotavital.requisicao.negocio.dominio;

import java.time.Duration;
import java.time.LocalDateTime;

/** Objeto de valor: prioridade da requisição. Define o prazo de entrega e se a justificativa é obrigatória (I4). */
public enum Prioridade {

    ROTINA(Duration.ofHours(24)),
    URGENCIA(Duration.ofHours(4)),
    EMERGENCIA(Duration.ofHours(1));

    private final Duration prazo;

    Prioridade(Duration prazo) {
        this.prazo = prazo;
    }

    /** Horário previsto de entrega: agora + prazo da prioridade (usado na invariante I2). */
    public LocalDateTime entregaPrevista(LocalDateTime agora) {
        return agora; // TODO verde: agora + prazo
    }

    public Duration prazo() {
        return prazo;
    }

    public boolean exigeJustificativa() {
        return false; // TODO verde: I4
    }

    public static Prioridade deNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Prioridade é obrigatória");
        }
        try {
            return valueOf(nome.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Prioridade inválida: " + nome);
        }
    }
}

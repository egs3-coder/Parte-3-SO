package br.cesar.rotavital.transporte.negocio.aplicacao;

import java.time.LocalDateTime;

/** Evento de integração publicado pelo Transporte quando a caixa chega ao destino. A Requisição consome. */
public record EntregaConcluida(String viagemId, String requisicaoId, LocalDateTime entregueEm) {
}

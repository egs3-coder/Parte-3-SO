# Spec: Reservar e liberar bolsa (contexto Estoque)

Revisão no squad: Lucas e Saulo.

## Objetivo

O Estoque é o dono da verdade das bolsas: decide se uma bolsa pode ser reservada para uma requisição e impede que
duas requisições levem a mesma bolsa.

## Linguagem

Bolsa, código da bolsa, hemocomponente, validade, reserva, bolsa disponível, bolsa vencida, descarte,
estoque crítico.

## Regras

- Só se reserva bolsa DISPONIVEL e dentro da validade ("Bolsa já reservada", "Bolsa vencida não pode ser reservada").
- A reserva guarda para qual requisição a bolsa foi reservada; só essa requisição pode liberá-la.
- Bolsa reservada não pode ser descartada.
- A validade depende do hemocomponente: hemácias 35 dias (CPDA-1), plasma fresco congelado 365 dias; vale até o fim do
  último dia.
- Estoque crítico (T4.1 da Parte 1): menos de 5 bolsas de hemácias O− ou O+, ou menos de 3 dos demais tipos.
- Concorrência: a linha da bolsa é travada (`SELECT … FOR UPDATE`) durante a reserva.

## Critérios

- Dada a bolsa disponível B-100, Quando a requisição REQ-001 reservar, Então a bolsa fica RESERVADA para REQ-001.
- Dada a bolsa reservada para REQ-001, Quando REQ-002 tentar reservar, Então "Bolsa já reservada".
- Dada uma bolsa vencida, Quando reservar, Então "Bolsa vencida não pode ser reservada".

## Eventos

Consome `DoacaoRegistrada` (Captação). Não publica eventos nesta fase.

## Fora do escopo

Expiração automática da reserva; rastreio ISBT 128 completo.

## Tarefas

- [x] Agregado `Bolsa` com teste
- [x] `ReservarBolsa` (interface pública usada pelo `EstoqueClient` da Requisição)
- [x] `GET /estoque`, `GET /estoque/criticos`, `GET /estoque/bolsas`, `POST /estoque/bolsas`

# Spec: Viagem e telemetria da caixa térmica (contexto Transporte)

Revisão no squad: Thiago e Eloi.

## Objetivo

Acompanhar a viagem da remessa e provar que a caixa ficou na faixa de temperatura. O sensor do fabricante manda a
temperatura em °F; o Rota Vital trabalha em °C.

## Linguagem

Viagem, leitura de telemetria, faixa de temperatura, excursão de temperatura, entrega.

## Regras

- A viagem nasce EM_TRANSITO com a faixa 2–6 °C (concentrado de hemácias, a única caixa desta fase).
- Leitura fora da faixa conta como excursão; a viagem informa quantas excursões teve.
- Leituras só são aceitas EM_TRANSITO; a entrega encerra a viagem (ENTREGUE) e publica `EntregaConcluida`.
- A faixa é um objeto de valor: mínimo menor que máximo.
- Camada anticorrupção: o payload do fabricante (`temperaturaF`, `capturadaEm`) é traduzido para `LeituraTelemetria`
  em °C na borda do contexto; o domínio nunca vê °F.

## Critérios

- Dada uma viagem em trânsito, Quando chega a leitura 41 °F, Então a leitura registrada é 5 °C, dentro da faixa.
- Quando chega 50 °F (10 °C), Então a viagem passa a ter 1 excursão de temperatura.
- Dada uma viagem entregue, Quando chega uma leitura, Então "Viagem já encerrada".

## Eventos

Publica `EntregaConcluida(viagemId, requisicaoId, entregueEm)`, consumido pela Requisição.

## Fora do escopo

MQTT/IoT Core e banco de séries temporais (cenário alto, ADR 0001); posição GPS.

## Tarefas

- [ ] Agregado `Viagem`, `FaixaTemperatura`, `LeituraTelemetria` com teste
- [ ] `TradutorSensorFabricante` (°F → °C)
- [ ] `POST /viagens/{id}/leituras`, `POST /viagens/{id}/entrega`, `GET /viagens`

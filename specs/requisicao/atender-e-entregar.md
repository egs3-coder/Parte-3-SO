# Spec: Atender, despachar e entregar a requisição (contexto Requisição, orquestrador)

Revisão no squad: Lucas e Saulo (camadas, saga e ADRs na Parte 2).

## Objetivo

Depois de alocar todas as bolsas, a atendente marca a requisição como atendida, a Expedição monta a remessa,
o Transporte abre a viagem e, quando a caixa chega, a requisição é encerrada. É a saga "Atender requisição" da
Parte 2 (D.3), rodando dentro do monolito: a Requisição comanda cada passo e guarda o estado.

## Linguagem

Requisição atendida, remessa, guia de remessa, viagem, entrega, cancelamento, liberação de reserva.

## Regras

- `marcarAtendida` só vale em EM_ATENDIMENTO e com todos os itens completos (I5).
- Ao ser atendida, a Requisição manda a Expedição montar a remessa (`ExpedicaoGateway.montarRemessa`) e guarda o
  número da guia.
- O despacho exige requisição ATENDIDA com guia; a Requisição manda o Transporte abrir a viagem
  (`TransporteGateway.abrirViagem`) e passa para EM_TRANSPORTE (ponto sem volta da saga).
- A entrega só é confirmada em EM_TRANSPORTE e encerra a requisição como ENTREGUE.
- Cancelar só é possível antes do transporte; o cancelamento libera no Estoque todas as bolsas alocadas
  (compensação) e exige motivo.

## Critérios

- Dada uma requisição com todos os itens completos, Quando a atendente marcar atendida, Então o status é ATENDIDA
  e existe uma remessa com guia "GR-<id>" contendo as bolsas alocadas.
- Dada uma requisição com item incompleto, Quando marcar atendida, Então recusa com
  "Requisição só fica atendida com todos os itens completos".
- Dada uma requisição atendida, Quando despachar, Então o status é EM_TRANSPORTE e existe a viagem "VG-<id>".
- Dada uma viagem em trânsito, Quando o motorista confirmar a entrega, Então a requisição fica ENTREGUE.
- Dada uma requisição em atendimento com bolsas alocadas, Quando cancelar com motivo, Então as bolsas voltam a ficar
  disponíveis no Estoque.

## Eventos

- Consumido: `EntregaConcluida` (Transporte). A Requisição escuta e chama `confirmarEntrega`.
- Os passos de Estoque, Expedição e Transporte são **comandos** pelas interfaces públicas (orquestração), como na
  saga da Parte 2; na projeção com microsserviços eles viram mensagens no broker.

## Fora do escopo

Compensação automática quando a Expedição encontra bolsa danificada (fica manual: cancelar e registrar nova
requisição); notificação ao hospital; outbox transacional (ADR 0002), desnecessário enquanto tudo roda numa transação.

## Tarefas

- [ ] `Requisicao.marcarAtendida`, `registrarRemessa`, `iniciarTransporte`, `confirmarEntrega`, `cancelar` com teste
- [ ] Casos de uso `AtenderRequisicao`, `DespacharRequisicao`, `CancelarRequisicao`
- [ ] Listener `ConfirmarEntregaAoConcluirViagem` (evento `EntregaConcluida`)
- [ ] Endpoints `POST /requisicoes/{id}/atendimento`, `/despacho`, `/cancelamento`
- [ ] Cenário de aceitação HTTP do fluxo completo (`src/test/resources/aceitacao/rota_vital.feature`)

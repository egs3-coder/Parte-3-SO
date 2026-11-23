# Spec: Alocar bolsa compatível (contexto Requisição)

Revisão no squad: João Ricardo e Pablo (domínio e regra ABO de plasma, como na Parte 2).

## Objetivo

A atendente do hemocentro (persona Carla, plantão noturno) aloca bolsas a cada item de uma requisição.
O sistema só aceita bolsas que o receptor pode receber e que continuam válidas quando chegarem ao hospital.
Enviar bolsa incompatível ou vencida é o erro mais caro do Rota Vital, por isso a regra fica na raiz do agregado
`Requisicao`, e não no controller nem no banco.

## Linguagem

Requisição, item da requisição, alocação de bolsa, bolsa reservada, hemocomponente (concentrado de hemácias,
plasma fresco congelado), tipo sanguíneo (grupo ABO + fator Rh), compatibilidade, validade, prioridade
(rotina, urgência, emergência), entrega prevista, justificativa clínica, unidade solicitante, reserva.

## Regras

Invariantes que a raiz `Requisicao` protege (numeração da Parte 2):

- **I1 Compatibilidade.** A bolsa precisa ser do mesmo hemocomponente do item ("Hemocomponente diferente do pedido")
  e compatível com o receptor pela regra desse hemocomponente ("Tipo sanguíneo incompatível"):
  - hemácias: os antígenos A, B e D da bolsa precisam existir no receptor (O− serve a todos; receptor Rh− só recebe Rh−);
  - plasma: regra invertida; os antígenos A e B do receptor precisam existir na bolsa (AB serve a todos) e o Rh não conta.
- **I2 Validade na entrega.** A bolsa precisa estar válida no horário previsto de entrega, que é agora + prazo da
  prioridade (rotina 24 h, urgência 4 h, emergência 1 h) ("Bolsa vence antes da entrega prevista").
- **I3 Quantidade.** Um item nunca recebe mais bolsas do que pediu ("Item já está completo"); a requisição tem ao menos
  um item; a mesma bolsa não entra duas vezes ("Bolsa já alocada nesta requisição").
- **I4 Emergência.** Requisição de emergência exige justificativa clínica.
- **I5 Ciclo de vida.** REGISTRADA → EM_ATENDIMENTO → ATENDIDA → EM_TRANSPORTE → ENTREGUE (ou CANCELADA antes do
  transporte). Só se aloca em EM_ATENDIMENTO.
- Uma recusa não altera nada: nem o status da requisição, nem as alocações existentes, nem a reserva no Estoque.
- A raiz é o único ponto de alteração: itens e alocações não têm métodos públicos que mudem estado.

Objetos de valor imutáveis, validados no construtor: `TipoSanguineo`, `Hemocomponente`, `Validade`, `CodigoBolsa`,
`Prioridade`, `Quantidade` (1 a 20), `JustificativaClinica` (até 500 caracteres), `RequisicaoId`, `ItemId`.

## Critérios

Viraram [`features/alocar_bolsa.feature`](../../features/alocar_bolsa.feature), o mesmo arquivo da Parte 2:

- Dado agora 24/11/2026 19:00 e a requisição REQ-001 de urgência com ITEM-1 (hemácias, receptor A+, 2 bolsas) e
  ITEM-2 (plasma, receptor A+, 2 bolsas):
  - Quando alocar hemácias O− válida ao ITEM-1, Então a alocação é aceita e o ITEM-1 tem 1 bolsa.
  - Quando alocar hemácias B+ ao ITEM-1, Então "Tipo sanguíneo incompatível".
  - Quando alocar plasma AB− ao ITEM-2, Então aceita; plasma O− ao ITEM-2, Então "Tipo sanguíneo incompatível".
  - Quando alocar plasma ao ITEM-1, Então "Hemocomponente diferente do pedido".
  - Quando alocar hemácias O− que vence às 21:00, Então "Bolsa vence antes da entrega prevista" (entrega às 23:00)
    e a requisição continua EM_ATENDIMENTO com 0 bolsas no item.

## Eventos

Nenhum publicado nesta funcionalidade. O caso de uso pede a reserva ao Estoque pela interface pública
(`EstoqueGateway.reservar`, comando ReservarBolsas da saga) e entrega a `BolsaReservada` ao agregado, como no
diagrama B.1 da Parte 2. Se o Estoque recusar (bolsa já reservada por outra requisição, vencida, inexistente), nada
é alocado. Se o agregado recusar, o caso de uso libera a reserva (`EstoqueGateway.liberar`, compensação), além de a
transação ser desfeita; assim a regra vale também nos testes sem banco.

> Revisão da spec (antes do código): a primeira versão consultava a bolsa e só reservava depois da alocação.
> Reservar primeiro usa o mesmo nome do diagrama da Parte 2 (`alocar(itemId, BolsaReservada)`) e evita uma
> segunda ida ao Estoque.

## Fora do escopo

Plaquetas e crioprecipitado (até a regra ser validada pelo responsável técnico do hemocentro); fenotipagem além de
ABO/Rh; expiração automática da reserva em 30 minutos; escolha automática da bolsa (FEFO) pelo sistema.

## Tarefas

- [ ] Objetos de valor com teste (`TipoSanguineo`, `Hemocomponente`, `Validade`, `Prioridade`, `Quantidade`, `JustificativaClinica`)
- [ ] Teste do agregado primeiro (vermelho), depois `Requisicao.alocar` (verde), depois refatoração
- [ ] Passos do Cucumber chamando o caso de uso `AlocarBolsa` (sem Spring, sem banco)
- [ ] `EstoqueGateway` (porta no domínio) e `EstoqueClient` (adaptador em dados)
- [ ] `POST /requisicoes/{id}/itens/{itemId}/alocacoes`

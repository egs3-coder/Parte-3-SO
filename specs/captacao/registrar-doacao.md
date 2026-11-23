# Spec: Registrar doação e dar entrada da bolsa no Estoque (Captação → Estoque, por evento)

Revisão no squad: Ewerton e Lucas (T4.1 e T4.2 da Parte 1).

## Objetivo

Manter as histórias H1 (triagem) e H2 (doação) da Parte 1, agora no contexto **Captação**, e fazer a bolsa
coletada entrar no **Estoque** sem que a Captação conheça as tabelas ou as classes do Estoque.

## Linguagem

Doador, triagem, apto/inapto, motivo de inaptidão, doação, número da doação (vira o código da bolsa), coleta,
bolsa, validade.

## Regras

- Só doa quem a triagem aprova (idade, peso, intervalo, limite anual: regras da Parte 1, sem mudança).
- O CPF é um objeto de valor: 11 dígitos e dígitos verificadores corretos (T2.5 da Parte 1, agora aplicada no cadastro).
- Cada doação recebe um número único (`RV-000001`), que é o código da bolsa (linguagem publicada entre os contextos).
- A Captação publica `DoacaoRegistrada`; o Estoque consome e registra a bolsa de concentrado de hemácias com validade
  de 35 dias (CPDA-1), disponível.
- O evento é tratado na mesma transação: se a entrada no Estoque falhar, a doação também não é gravada.

## Critérios

- Dado um doador apto, Quando ele doa, Então a doação é registrada e o Estoque passa a ter a bolsa com o mesmo
  código, do tipo do doador, válida por 35 dias.
- Dado um doador que doou há 30 dias, Quando ele tenta doar, Então a doação é recusada com o motivo do intervalo e
  nenhuma bolsa entra no Estoque.

## Eventos

- Publicado por Captação: `DoacaoRegistrada(codigoBolsa, tipoSanguineo, dataColeta)`.
- Consumido por Estoque: `EntradaDeBolsaPorDoacao` (`@EventListener`, em memória, `ApplicationEventPublisher`).

## Fora do escopo

Fracionamento do sangue total em plasma e plaquetas (bolsas de plasma entram pelo cadastro direto do Estoque);
fila externa (o evento é em memória; ADR 0002 descreve o outbox para quando houver broker).

## Tarefas

- [ ] Mover as regras de triagem da Parte 1 para `captacao.negocio.dominio`, com os mesmos testes
- [ ] `Cpf` como objeto de valor validado no construtor
- [ ] `RegistrarDoacao` publica `DoacaoRegistrada`
- [ ] `EntradaDeBolsaPorDoacao` no Estoque
- [ ] Cenário de aceitação HTTP adaptado (`src/test/resources/aceitacao/doacao.feature`)

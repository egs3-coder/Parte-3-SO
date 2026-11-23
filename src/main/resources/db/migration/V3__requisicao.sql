-- Contexto Requisição (core): o agregado Requisição em três tabelas do schema "requisicao".
-- A bolsa alocada é uma cópia na linguagem da Requisição (código, hemocomponente, tipo, validade):
-- não há chave estrangeira para estoque.bolsa, e a Requisição nunca lê aquela tabela.

CREATE SEQUENCE requisicao.requisicao_seq;

CREATE TABLE requisicao.requisicao (
    id                  VARCHAR(30)  PRIMARY KEY,
    unidade_solicitante VARCHAR(20)  NOT NULL,
    prioridade          VARCHAR(12)  NOT NULL,
    justificativa       VARCHAR(500) NOT NULL DEFAULT '',
    registrada_em       TIMESTAMP    NOT NULL,
    status              VARCHAR(16)  NOT NULL,
    guia_remessa        VARCHAR(50),
    viagem_id           VARCHAR(40),
    motivo_cancelamento VARCHAR(500)
);

CREATE INDEX idx_requisicao_status ON requisicao.requisicao (status);

CREATE TABLE requisicao.item (
    requisicao_id  VARCHAR(30) NOT NULL REFERENCES requisicao.requisicao (id) ON DELETE CASCADE,
    item_id        VARCHAR(12) NOT NULL,
    hemocomponente VARCHAR(30) NOT NULL,
    tipo_receptor  VARCHAR(12) NOT NULL,
    quantidade     INTEGER     NOT NULL CHECK (quantidade BETWEEN 1 AND 20),
    PRIMARY KEY (requisicao_id, item_id)
);

CREATE TABLE requisicao.alocacao (
    requisicao_id  VARCHAR(30) NOT NULL,
    item_id        VARCHAR(12) NOT NULL,
    codigo_bolsa   VARCHAR(30) NOT NULL,
    hemocomponente VARCHAR(30) NOT NULL,
    tipo_sanguineo VARCHAR(12) NOT NULL,
    validade       TIMESTAMP   NOT NULL,
    alocada_em     TIMESTAMP   NOT NULL,
    PRIMARY KEY (requisicao_id, codigo_bolsa),
    FOREIGN KEY (requisicao_id, item_id) REFERENCES requisicao.item (requisicao_id, item_id) ON DELETE CASCADE
);

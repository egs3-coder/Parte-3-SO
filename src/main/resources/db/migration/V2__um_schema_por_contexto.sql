-- Parte 3 (DDD): cada bounded context ganha o seu schema e só ele lê e escreve nas suas tabelas.
-- Um contexto que precisa de algo de outro chama a interface pública (camada de aplicação) ou reage a um evento;
-- não existe chave estrangeira entre schemas.
--
-- As tabelas da Parte 1 (doador, doacao, bolsa no schema public) só tinham dados sintéticos de demonstração,
-- e o modelo mudou (bolsa com código, hemocomponente e reserva; CPF validado pelos dígitos verificadores).
-- Por isso elas são removidas aqui, e não migradas linha a linha.
DROP TABLE IF EXISTS doacao;
DROP TABLE IF EXISTS bolsa;
DROP TABLE IF EXISTS doador;

CREATE SCHEMA IF NOT EXISTS captacao;
CREATE SCHEMA IF NOT EXISTS estoque;
CREATE SCHEMA IF NOT EXISTS rede;
CREATE SCHEMA IF NOT EXISTS requisicao;
CREATE SCHEMA IF NOT EXISTS expedicao;
CREATE SCHEMA IF NOT EXISTS transporte;

-- ---------- Captação ----------
CREATE TABLE captacao.doador (
    id                        BIGSERIAL PRIMARY KEY,
    nome                      VARCHAR(120)     NOT NULL,
    cpf                       VARCHAR(11)      NOT NULL UNIQUE,
    data_nascimento           DATE             NOT NULL,
    sexo                      VARCHAR(10)      NOT NULL,
    peso_kg                   DOUBLE PRECISION NOT NULL,
    tipo_sanguineo            VARCHAR(12)      NOT NULL,
    consentimento_responsavel BOOLEAN          NOT NULL DEFAULT FALSE
);

CREATE TABLE captacao.doacao (
    id             BIGSERIAL PRIMARY KEY,
    doador_id      BIGINT      NOT NULL REFERENCES captacao.doador (id),
    numero         VARCHAR(30) NOT NULL UNIQUE,
    tipo_sanguineo VARCHAR(12) NOT NULL,
    data_coleta    DATE        NOT NULL
);

CREATE INDEX idx_doacao_doador ON captacao.doacao (doador_id);
CREATE SEQUENCE captacao.doacao_numero_seq;

-- ---------- Estoque ----------
CREATE TABLE estoque.bolsa (
    codigo         VARCHAR(30) PRIMARY KEY,
    hemocomponente VARCHAR(30) NOT NULL,
    tipo_sanguineo VARCHAR(12) NOT NULL,
    data_coleta    DATE        NOT NULL,
    validade       TIMESTAMP   NOT NULL,
    status         VARCHAR(12) NOT NULL,
    reservada_para VARCHAR(30),
    versao         BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX idx_bolsa_status_validade ON estoque.bolsa (status, validade);

-- ---------- Rede ----------
CREATE TABLE rede.unidade (
    codigo VARCHAR(20)  PRIMARY KEY,
    nome   VARCHAR(120) NOT NULL,
    cidade VARCHAR(80)  NOT NULL,
    uf     VARCHAR(2)   NOT NULL,
    ativa  BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ---------- Expedição ----------
CREATE TABLE expedicao.remessa (
    guia          VARCHAR(50) PRIMARY KEY,
    requisicao_id VARCHAR(30) NOT NULL UNIQUE,
    destino       VARCHAR(20) NOT NULL,
    montada_em    TIMESTAMP   NOT NULL,
    status        VARCHAR(12) NOT NULL
);

CREATE TABLE expedicao.remessa_bolsa (
    guia         VARCHAR(50) NOT NULL REFERENCES expedicao.remessa (guia),
    posicao      INTEGER     NOT NULL,
    codigo_bolsa VARCHAR(30) NOT NULL,
    PRIMARY KEY (guia, posicao)
);

-- ---------- Transporte ----------
CREATE TABLE transporte.viagem (
    id             VARCHAR(40)      PRIMARY KEY,
    requisicao_id  VARCHAR(30)      NOT NULL UNIQUE,
    guia           VARCHAR(50)      NOT NULL,
    faixa_minima_c DOUBLE PRECISION NOT NULL,
    faixa_maxima_c DOUBLE PRECISION NOT NULL,
    iniciada_em    TIMESTAMP        NOT NULL,
    status         VARCHAR(12)      NOT NULL,
    entregue_em    TIMESTAMP
);

CREATE TABLE transporte.leitura (
    viagem_id     VARCHAR(40)      NOT NULL REFERENCES transporte.viagem (id),
    posicao       INTEGER          NOT NULL,
    capturada_em  TIMESTAMP        NOT NULL,
    temperatura_c DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (viagem_id, posicao)
);

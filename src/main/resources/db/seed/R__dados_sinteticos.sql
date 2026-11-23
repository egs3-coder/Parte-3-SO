-- Dados sintéticos para a demonstração (não entram nos testes de integração).
-- Migração repetível: roda depois das versionadas e só de novo se este arquivo mudar. Idempotente.

INSERT INTO rede.unidade (codigo, nome, cidade, uf, ativa) VALUES
    ('HSA', 'Hospital Sintético A', 'Recife',   'PE', TRUE),
    ('HSB', 'Hospital Sintético B', 'Olinda',   'PE', TRUE),
    ('HSC', 'Hospital Sintético C', 'Caruaru',  'PE', TRUE)
ON CONFLICT (codigo) DO NOTHING;

-- Bolsas de concentrado de hemácias (35 dias) e plasma fresco congelado (365 dias), válidas até o fim do dia.
INSERT INTO estoque.bolsa (codigo, hemocomponente, tipo_sanguineo, data_coleta, validade, status) VALUES
    ('B-001', 'CONCENTRADO_HEMACIAS',    'O_NEGATIVO',  CURRENT_DATE - 3,  (CURRENT_DATE + 32)  + TIME '23:59', 'DISPONIVEL'),
    ('B-002', 'CONCENTRADO_HEMACIAS',    'O_NEGATIVO',  CURRENT_DATE - 10, (CURRENT_DATE + 25)  + TIME '23:59', 'DISPONIVEL'),
    ('B-003', 'CONCENTRADO_HEMACIAS',    'O_POSITIVO',  CURRENT_DATE - 5,  (CURRENT_DATE + 30)  + TIME '23:59', 'DISPONIVEL'),
    ('B-004', 'CONCENTRADO_HEMACIAS',    'A_POSITIVO',  CURRENT_DATE - 2,  (CURRENT_DATE + 33)  + TIME '23:59', 'DISPONIVEL'),
    ('B-005', 'CONCENTRADO_HEMACIAS',    'A_POSITIVO',  CURRENT_DATE - 20, (CURRENT_DATE + 15)  + TIME '23:59', 'DISPONIVEL'),
    ('B-006', 'CONCENTRADO_HEMACIAS',    'B_POSITIVO',  CURRENT_DATE - 7,  (CURRENT_DATE + 28)  + TIME '23:59', 'DISPONIVEL'),
    ('B-007', 'CONCENTRADO_HEMACIAS',    'AB_POSITIVO', CURRENT_DATE - 1,  (CURRENT_DATE + 34)  + TIME '23:59', 'DISPONIVEL'),
    ('P-001', 'PLASMA_FRESCO_CONGELADO', 'AB_NEGATIVO', CURRENT_DATE - 30, (CURRENT_DATE + 335) + TIME '23:59', 'DISPONIVEL'),
    ('P-002', 'PLASMA_FRESCO_CONGELADO', 'A_POSITIVO',  CURRENT_DATE - 15, (CURRENT_DATE + 350) + TIME '23:59', 'DISPONIVEL'),
    ('P-003', 'PLASMA_FRESCO_CONGELADO', 'O_NEGATIVO',  CURRENT_DATE - 40, (CURRENT_DATE + 325) + TIME '23:59', 'DISPONIVEL')
ON CONFLICT (codigo) DO NOTHING;

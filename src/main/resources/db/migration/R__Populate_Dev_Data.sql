-- RESPONSABILIDADE: Limpar e popular o banco com dados de desenvolvimento.
-- Sendo uma migração REPETÍVEL (R__), o Flyway a executará sempre que o seu conteúdo for alterado.

-- 1. LIMPEZA COMPLETA DAS TABELAS (EM ORDEM DE DEPENDÊNCIA)
TRUNCATE TABLE
    estoques,
    precos,
    composicao_produto,
    movimentacoes_estoque_produto,
    movimentacoes_estoque_lote,
    lotes_materia_prima,
    tipos_materia_prima,
    produtos,
    canais_venda
    CASCADE;

-- 2. RESET DAS SEQUÊNCIAS DE ID PARA PREVISIBILIDADE NOS TESTES
ALTER SEQUENCE tipos_materia_prima_id_seq RESTART WITH 1;
ALTER SEQUENCE lotes_materia_prima_id_seq RESTART WITH 1;
ALTER SEQUENCE movimentacoes_estoque_lote_id_seq RESTART WITH 1;
ALTER SEQUENCE produtos_id_seq RESTART WITH 1;
ALTER SEQUENCE movimentacoes_estoque_produto_id_seq RESTART WITH 1;
ALTER SEQUENCE composicao_produto_id_seq RESTART WITH 1;
ALTER SEQUENCE precos_id_seq RESTART WITH 1;
ALTER SEQUENCE canais_venda_id_seq RESTART WITH 1;
ALTER SEQUENCE estoques_id_seq RESTART WITH 1;

-- 3. INSERÇÃO DE DADOS DE DESENVOLVIMENTO

-- ETAPA A: INSERIR TIPOS DE MATÉRIAS-PRIMAS (O CATÁLOGO)
INSERT INTO tipos_materia_prima (nome, unidade_de_consumo) VALUES
                                                               ('Adesivo Kraft Pardo', 'CENTIMETRO_QUADRADO'),
                                                               ('Adesivo Vinil Branco Brilho', 'CENTIMETRO_QUADRADO');

-- ETAPA B: INSERIR LOTES FÍSICOS NO ESTOQUE
INSERT INTO lotes_materia_prima (tipo_materia_prima_id, unidade_de_estoque, atributos) VALUES
                                                                                           (1, 'METRO_LINEAR', '{ "larguraMm": 300 }'),
                                                                                           (2, 'METRO_LINEAR', '{ "larguraMm": 500 }');

-- ETAPA C: REGISTAR AS ENTRADAS DE ESTOQUE DE INSUMOS
INSERT INTO movimentacoes_estoque_lote (lote_id, data, tipo, quantidade, motivo) VALUES
                                                                                     (1, NOW(), 'ENTRADA_COMPRA', 50.00, 'Nota Fiscal #1001'),
                                                                                     (2, NOW(), 'ENTRADA_COMPRA', 100.00, 'Nota Fiscal #1003');

-- ETAPA D: INSERIR CANAIS DE VENDA
INSERT INTO canais_venda (nome) VALUES ('LOJA_FISICA'), ('SHOPEE');

-- ETAPA E: INSERIR PRODUTOS ACABADOS
INSERT INTO produtos (nome, sku, descricao, cor, unidades_por_produto, ativo) VALUES
                                                                                  ('Etiqueta Redonda Kraft 5x5cm', 'ETQ-KFT-RD-50', 'Pacote com 100 etiquetas.', 'Pardo', 100, true),
                                                                                  ('Etiqueta Retangular Vinil 9x5cm', 'ETQ-VNL-RT-95', 'Pacote com 100 etiquetas.', 'Branco', 100, true);

-- ETAPA F: MONTAR A "RECEITA" (COMPOSIÇÃO) DOS PRODUTOS
INSERT INTO composicao_produto (produto_id, tipo_materia_prima_id, gasto_material_por_unidade) VALUES
                                                                                                   (1, 1, 25.0000),
                                                                                                   (2, 2, 45.0000);

-- ETAPA G: DEFINIR O ESTOQUE MESTRE INICIAL (USANDO A NOVA TABELA)
INSERT INTO movimentacoes_estoque_produto (produto_id, data, tipo, quantidade, motivo) VALUES
                                                                                           (1, NOW(), 'AJUSTE_MANUAL', 60, 'Carga inicial de estoque'),
                                                                                           (2, NOW(), 'AJUSTE_MANUAL', 50, 'Carga inicial de estoque');

-- ETAPA H: DEFINIR O ESTOQUE DISTRIBUÍDO (QUE AGORA TEM UM LIMITE)
INSERT INTO estoques (produto_id, canal_venda_id, quantidade) VALUES
                                                                  (1, 1, 10), -- 10 na Loja Física
                                                                  (1, 2, 50), -- 50 na Shopee (Total 60, bate certo)
                                                                  (2, 1, 20), -- 20 na Loja Física (Total 20 de 50, ainda sobram 30 para outros canais)
                                                                  (2, 2, 0);  -- Zero na Shopee

-- ETAPA I: DEFINIR OS PREÇOS DOS PRODUTOS
INSERT INTO precos (produto_id, tipo_preco, valor) VALUES
                                                       (1, 'VAREJO', 25.00),
                                                       (2, 'VAREJO', 35.00);


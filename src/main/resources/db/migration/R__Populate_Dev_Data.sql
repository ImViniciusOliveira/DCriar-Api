-- RESPONSABILIDADE: Limpar e popular o banco com um conjunto de dados de desenvolvimento rico e realista.
-- Sendo uma migração REPETÍVEL (R__), o Flyway a executará sempre que o seu conteúdo for alterado.

-- 1. LIMPEZA COMPLETA DAS TABELAS (EM ORDEM DE DEPENDÊNCIA)
-- A ordem é da tabela que é referenciada para a que referencia (de filho para pai, em termos de FK)
TRUNCATE TABLE
    sale_items,
    sales,
    estoques,
    precos,
    cortes_realizados,
    ordens_de_corte,
    movimentacoes_estoque_produto,
    movimentacoes_estoque_lote,
    lotes_materia_prima,
    produtos,
    tipos_materia_prima,
    canais_venda
    CASCADE;

-- 2. RESET DAS SEQUÊNCIAS DE ID
ALTER SEQUENCE tipos_materia_prima_id_seq RESTART WITH 1;
ALTER SEQUENCE lotes_materia_prima_id_seq RESTART WITH 1;
ALTER SEQUENCE movimentacoes_estoque_lote_id_seq RESTART WITH 1;
ALTER SEQUENCE produtos_id_seq RESTART WITH 1;
ALTER SEQUENCE movimentacoes_estoque_produto_id_seq RESTART WITH 1;
ALTER SEQUENCE precos_id_seq RESTART WITH 1;
ALTER SEQUENCE canais_venda_id_seq RESTART WITH 1;
ALTER SEQUENCE estoques_id_seq RESTART WITH 1;
ALTER SEQUENCE sales_id_seq RESTART WITH 1;
ALTER SEQUENCE sale_items_id_seq RESTART WITH 1;
ALTER SEQUENCE ordens_de_corte_id_seq RESTART WITH 1;
ALTER SEQUENCE cortes_realizados_id_seq RESTART WITH 1;

-- 3. INSERÇÃO DE DADOS DE DESENVOLVIMENTO

-- ETAPA A: INSERIR TIPOS DE MATÉRIAS-PRIMAS
INSERT INTO tipos_materia_prima (nome, unidade_de_consumo) VALUES
    ('Adesivo Kraft Pardo', 'CENTIMETRO_QUADRADO'),
    ('Adesivo Vinil Branco Brilho', 'METRO_QUADRADO'),
    ('Adesivo Vinil Transparente Fosco', 'METRO_LINEAR'),
    ('Papel Couchê', 'CENTIMETRO_LINEAR'),
    ('Vinil Fosco', 'QUILOGRAMA'),
    ('Vinil Brilho', 'GRAMA'),
    ('Papel Adesivo', 'LITRO'),
    ('Filme BOPP', 'MILILITRO'),
    ('Papel Offset', 'UNIDADE'),
    ('Papel Reciclado', 'OUTROS');

-- ETAPA B: INSERIR LOTES FÍSICOS NO ESTOQUE
INSERT INTO lotes_materia_prima (tipo_materia_prima_id, unidade_de_estoque, atributos, lote_de_origem_id) VALUES
    (1, 'CENTIMETRO_QUADRADO', '{ "larguraMm": 300 }', null), -- Lote ID 1
    (2, 'METRO_QUADRADO', '{ "larguraMm": 500 }', null), -- Lote ID 2
    (3, 'METRO_LINEAR', '{ "larguraMm": 500 }', null), -- Lote ID 3
    (4, 'CENTIMETRO_LINEAR', '{ "larguraMm": 400 }', null), -- Lote ID 4
    (5, 'QUILOGRAMA', '{ "pesoKg": 10 }', null), -- Lote ID 5
    (6, 'GRAMA', '{ "pesoG": 500 }', null), -- Lote ID 6
    (7, 'LITRO', '{ "volumeL": 20 }', null), -- Lote ID 7
    (8, 'MILILITRO', '{ "volumeMl": 2000 }', null), -- Lote ID 8
    (9, 'UNIDADE', '{ "quantidade": 100 }', null), -- Lote ID 9
    (10, 'OUTROS', '{ "descricao": "Material especial" }', null); -- Lote ID 10

-- ETAPA C: REGISTRAR AS ENTRADAS DE ESTOQUE DE INSUMOS
INSERT INTO movimentacoes_estoque_lote (lote_id, data, tipo, quantidade, motivo, custo_por_unidade_base) VALUES
    (1, NOW() - INTERVAL '10 day', 'ENTRADA_COMPRA', 50.00, 'Nota Fiscal #1001', 0.000667),
    (2, NOW() - INTERVAL '8 day', 'ENTRADA_COMPRA', 100.00, 'Nota Fiscal #1003', 0.000500),
    (3, NOW() - INTERVAL '5 day', 'ENTRADA_COMPRA', 75.00, 'Nota Fiscal #1004', 0.000550),
    (4, NOW() - INTERVAL '1 day', 'ENTRADA_SOBRA', 2.60, 'Sobra da Ordem de Corte #2', 0.000500);

-- ETAPA D: INSERIR CANAIS DE VENDA
INSERT INTO canais_venda (nome) VALUES
    ('LOJA_FISICA'),      -- ID 1
    ('SHOPEE'),           -- ID 2
    ('SITE_PROPRIO'),     -- ID 3
    ('MERCADO_LIVRE');    -- ID 4

-- ETAPA E: INSERIR PRODUTOS ACABADOS (AGORA COMO "MOLDES")
INSERT INTO produtos (nome, sku, descricao, cor, unidades_por_produto, ativo, foto_principal_url, tipo_materia_prima_id, largura_cm_unitaria, comprimento_cm_unitario) VALUES
    ('Etiqueta Kraft 5x5cm', 'ETQ-KFT-5X5', 'Etiqueta em papel kraft 5x5cm', 'Pardo', 100, true, '', 1, 5.0, 5.0),
    ('Etiqueta Vinil Brilho 9x5cm', 'ETQ-VNL-BR-9X5', 'Etiqueta vinil branco brilho 9x5cm', 'Branco', 100, true, '', 2, 9.0, 5.0),
    ('Etiqueta Vinil Fosco 3x3cm', 'ETQ-VNL-FS-3X3', 'Etiqueta vinil fosco 3x3cm', 'Transparente', 100, true, '', 3, 3.0, 3.0),
    ('Etiqueta Couchê 4x4cm', 'ETQ-COU-4X4', 'Etiqueta papel couchê 4x4cm', 'Branco', 100, true, '', 4, 4.0, 4.0),
    ('Etiqueta BOPP 6x6cm', 'ETQ-BOPP-6X6', 'Etiqueta filme BOPP 6x6cm', 'Transparente', 100, true, '', 8, 6.0, 6.0),
    ('Etiqueta Offset 7x7cm', 'ETQ-OFF-7X7', 'Etiqueta papel offset 7x7cm', 'Branco', 100, true, '', 9, 7.0, 7.0),
    ('Etiqueta Reciclado 8x8cm', 'ETQ-REC-8X8', 'Etiqueta papel reciclado 8x8cm', 'Pardo', 100, true, '', 10, 8.0, 8.0),
    ('Etiqueta Adesivo 10x10cm', 'ETQ-ADS-10X10', 'Etiqueta papel adesivo 10x10cm', 'Branco', 100, true, '', 7, 10.0, 10.0),
    ('Etiqueta Especial 12x12cm', 'ETQ-ESP-12X12', 'Etiqueta especial 12x12cm', 'Colorido', 100, true, '', 5, 12.0, 12.0),
    ('Etiqueta Genérica 15x15cm', 'ETQ-GEN-15X15', 'Etiqueta genérica 15x15cm', 'Branco', 100, true, '', 6, 15.0, 15.0);

-- ETAPA F: DEFINIR O ESTOQUE MESTRE INICIAL (Estoque Lógico Central)
-- Para cada produto, define um estoque físico inicial de 100 unidades.
DO $$
DECLARE
    i INT;
BEGIN
    FOR i IN 1..10 LOOP -- Para cada um dos 10 produtos inseridos acima
        INSERT INTO movimentacoes_estoque_produto (produto_id, data, tipo, quantidade, motivo)
        VALUES (i, NOW(), 'AJUSTE_MANUAL', 100, 'Carga inicial de estoque de desenvolvimento');
    END LOOP;
END $$;

-- ETAPA G: DEFINIR O ESTOQUE DISTRIBUÍDO (Físico, por canal)
-- Para cada produto, distribui o estoque inicial de 100 unidades igualmente entre os 4 canais.
DO $$
DECLARE
    p_id INT;
    c_id INT;
BEGIN
    FOR p_id IN 1..10 LOOP -- Para cada produto
        FOR c_id IN 1..4 LOOP -- Para cada canal de venda
            INSERT INTO estoques (produto_id, canal_venda_id, quantidade)
            VALUES (p_id, c_id, 25); -- 25 unidades em cada canal
        END LOOP;
    END LOOP;
END $$;

-- ETAPA H: DEFINIR OS PREÇOS
INSERT INTO precos (produto_id, tipo_preco, valor, valor_promocional, promocao_ativa) VALUES
    (1, 'VAREJO', 25.00, null, false),
    (2, 'VAREJO', 35.00, 29.90, true), -- Preço promocional ativo!
    (3, 'VAREJO', 22.00, null, false);

-- ETAPA I: INSERIR ORDENS DE CORTE DE EXEMPLO
INSERT INTO ordens_de_corte (produto_id, lote_principal_id, quantidade_produzida, modo_calculo, largura_final_cm, comprimento_final_cm, data_criacao, motivo, canal_venda_destino_id, margem_superior_cm, margem_inferior_cm, margem_esquerda_cm, margem_direita_cm) VALUES
    (1, 1, 100, 'AUTOMATICO', 5.2, 500.4, NOW() - INTERVAL '2 day', 'PEDIDO-SHP-101', 2, 0.2, 0.2, 0.1, 0.1);

INSERT INTO ordens_de_corte (produto_id, lote_principal_id, quantidade_produzida, modo_calculo, largura_final_cm, comprimento_final_cm, data_criacao, motivo, canal_venda_destino_id, margem_superior_cm, margem_inferior_cm, margem_esquerda_cm, margem_direita_cm) VALUES
    (2, 2, 50, 'MANUAL', 10.0, 260.0, NOW() - INTERVAL '1 day', 'PEDIDO-LJA-205', 1, 0.0, 0.0, 0.0, 0.0);

-- ETAPA J: INSERIR CORTES REALIZADOS DE EXEMPLO
INSERT INTO cortes_realizados (ordem_de_corte_id, largura_cm, comprimento_cm, quantidade, tipo) VALUES
    (1, 5.0, 5.0, 100, 'PRODUTO'),
    (2, 9.0, 5.0, 50, 'PRODUTO'),
    (2, 1.0, 260.0, 1, 'SOBRA'); -- Sobra que gerou o lote de retalho ID 4

-- ETAPA K: INSERIR VENDAS DE EXEMPLO
-- Venda 1: Na Shopee, cliente comprou 2 produtos diferentes
INSERT INTO sales (sale_date, canal_venda_id, total_amount) VALUES
    (NOW() - INTERVAL '1 day', 2, 64.90); -- Venda ID 1

INSERT INTO sale_items (sale_id, produto_id, quantity, unit_price, total_price) VALUES
    (1, 1, 1, 25.00, 25.00), -- Preço normal
    (1, 2, 1, 29.90, 29.90); -- Preço promocional aplicado

-- Venda 2: Na Loja Física, cliente comprou 3 unidades do mesmo produto
INSERT INTO sales (sale_date, canal_venda_id, total_amount) VALUES
    (NOW(), 1, 75.00); -- Venda ID 2

INSERT INTO sale_items (sale_id, produto_id, quantity, unit_price, total_price) VALUES
    (2, 1, 3, 25.00, 75.00);

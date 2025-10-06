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
    ('Adesivo Vinil Branco Brilho', 'CENTIMETRO_QUADRADO'),
    ('Adesivo Vinil Transparente Fosco', 'CENTIMETRO_QUADRADO');

-- ETAPA B: INSERIR LOTES FÍSICOS NO ESTOQUE
-- Lotes principais comprados de fornecedores
INSERT INTO lotes_materia_prima (tipo_materia_prima_id, unidade_de_estoque, atributos, lote_de_origem_id) VALUES
    (1, 'METRO_LINEAR', '{ "larguraMm": 300 }', null), -- Lote ID 1
    (2, 'METRO_LINEAR', '{ "larguraMm": 500 }', null), -- Lote ID 2
    (3, 'METRO_LINEAR', '{ "larguraMm": 500 }', null); -- Lote ID 3
-- Lote de retalho/sobra gerado internamente a partir de uma ordem de corte
INSERT INTO lotes_materia_prima (tipo_materia_prima_id, unidade_de_estoque, atributos, lote_de_origem_id) VALUES
    (2, 'METRO_LINEAR', '{ "larguraMm": 100 }', 2); -- Lote ID 4 (Retalho do Lote 2)

-- ETAPA C: REGISTRAR AS ENTRADAS DE ESTOQUE DE INSUMOS
INSERT INTO movimentacoes_estoque_lote (lote_id, data, tipo, quantidade, motivo, custo_por_unidade_base) VALUES
    (1, NOW() - INTERVAL '10 day', 'ENTRADA_COMPRA', 50.00, 'Nota Fiscal #1001', 0.000667),
    (2, NOW() - INTERVAL '8 day', 'ENTRADA_COMPRA', 100.00, 'Nota Fiscal #1003', 0.000500),
    (3, NOW() - INTERVAL '5 day', 'ENTRADA_COMPRA', 75.00, 'Nota Fiscal #1004', 0.000550),
    (4, NOW() - INTERVAL '1 day', 'ENTRADA_SOBRA', 2.60, 'Sobra da Ordem de Corte #2', 0.000500); -- Custo herdado do lote pai

-- ETAPA D: INSERIR CANAIS DE VENDA
INSERT INTO canais_venda (nome) VALUES
    ('LOJA_FISICA'),      -- ID 1
    ('SHOPEE'),           -- ID 2
    ('SITE_PROPRIO'),     -- ID 3
    ('MERCADO_LIVRE');    -- ID 4

-- ETAPA E: INSERIR PRODUTOS ACABADOS (AGORA COMO "MOLDES")
INSERT INTO produtos (nome, sku, descricao, cor, unidades_por_produto, ativo, foto_principal_url, tipo_materia_prima_id, largura_cm_unitaria, comprimento_cm_unitario) VALUES
    ('Etiqueta Redonda Kraft 5x5cm', 'ETQ-KFT-RD-50', 'Pacote com 100 etiquetas adesivas em papel kraft.', 'Pardo', 100, true, '', 1, 5.0, 5.0),
    ('Etiqueta Retangular Vinil 9x5cm', 'ETQ-VNL-RT-95', 'Pacote com 100 etiquetas de vinil branco para diversas finalidades.', 'Branco', 100, true, '', 2, 9.0, 5.0),
    ('Adesivo Transparente Coração 3x3cm', 'ETQ-TRN-CR-30', 'Pacote com 100 adesivos de coração em vinil transparente.', 'Transparente', 100, true, '', 3, 3.0, 3.0);

-- ETAPA F: DEFINIR O ESTOQUE MESTRE INICIAL (Estoque Lógico Central)
INSERT INTO movimentacoes_estoque_produto (produto_id, data, tipo, quantidade, motivo) VALUES
    (1, NOW(), 'AJUSTE_MANUAL', 60, 'Carga inicial de estoque'),
    (2, NOW(), 'AJUSTE_MANUAL', 50, 'Carga inicial de estoque'),
    (3, NOW(), 'ENTRADA_PRODUCAO', 80, 'Produção para estoque inicial');

-- ETAPA G: DEFINIR O ESTOQUE DISTRIBUÍDO (Físico, por canal)
INSERT INTO estoques (produto_id, canal_venda_id, quantidade) VALUES
    -- Produto 1
    (1, 1, 10), (1, 2, 50),
    -- Produto 2
    (2, 1, 20), (2, 2, 0), (2, 4, 30),
    -- Produto 3
    (3, 3, 80);

-- ETAPA H: DEFINIR OS PREÇOS
INSERT INTO precos (produto_id, tipo_preco, valor, valor_promocional, promocao_ativa) VALUES
    (1, 'VAREJO', 25.00, null, false),
    (2, 'VAREJO', 35.00, 29.90, true), -- Preço promocional ativo!
    (3, 'VAREJO', 22.00, null, false);

-- ETAPA I: INSERIR ORDENS DE CORTE DE EXEMPLO
-- Ordem Automática: O tamanho final é o tamanho do produto + margens.
INSERT INTO ordens_de_corte (produto_id, lote_principal_id, quantidade_produzida, modo_calculo, largura_final_cm, comprimento_final_cm, data_criacao, motivo, canal_venda_destino_id, margem_superior_cm, margem_inferior_cm, margem_esquerda_cm, margem_direita_cm) VALUES
    (1, 1, 100, 'AUTOMATICO', 5.2, 500.4, NOW() - INTERVAL '2 day', 'PEDIDO-SHP-101', 2, 0.2, 0.2, 0.1, 0.1);

-- Ordem Manual: O tamanho final é informado pelo operador, e as margens são ignoradas (zeradas).
INSERT INTO ordens_de_corte (produto_id, lote_principal_id, quantidade_produzida, modo_calculo, largura_final_cm, comprimento_final_cm, data_criacao, motivo, canal_venda_destino_id, margem_superior_cm, margem_inferior_cm, margem_esquerda_cm, margem_direita_cm) VALUES
    (2, 2, 50, 'MANUAL', 10.0, 260.0, NOW() - INTERVAL '1 day', 'PEDIDO-LJA-205', 1, 0.0, 0.0, 0.0, 0.0);

-- ETAPA J: INSERIR CORTES REALIZADOS DE EXEMPLO
INSERT INTO cortes_realizados (ordem_de_corte_id, largura_cm, comprimento_cm, quantidade, tipo) VALUES
    (1, 5.0, 5.0, 100, 'PRODUTO'),
    (2, 9.0, 5.0, 50, 'PRODUTO'),
    (2, 1.0, 260.0, 1, 'SOBRA'); -- Sobra que gerou o lote de retalho ID 4

-- ETAPA K: INSERIR VENDAS DE EXEMPLO (DADOS QUE ESTAVAM FALTANDO)
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

-- RESPONSABILIDADE: Limpar e popular o banco com um conjunto de dados de desenvolvimento rico e realista.
-- Sendo uma migração REPETÍVEL (R__), o Flyway a executará sempre que o seu conteúdo for alterado.

-- 1. LIMPEZA COMPLETA DAS TABELAS (EM ORDEM DE DEPENDÊNCIA REVERSA)
TRUNCATE TABLE
    sale_items, sales, estoques, precos, cortes_realizados, ordens_de_corte,
    movimentacoes_estoque_produto, movimentacoes_estoque_lote, lotes_materia_prima,
    produtos, tipos_materia_prima, canais_venda
    CASCADE;

-- 2. RESET DAS SEQUÊNCIAS DE ID (MÉTODO LIMPO, SEM OUTPUT NO LOG)
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

-- ETAPA A: TIPOS DE MATÉRIAS-PRIMAS MAIS REALISTAS
INSERT INTO tipos_materia_prima (nome, unidade_de_consumo) VALUES
    ('Papel Couchê 300g', 'UNIDADE'),                     -- ID 1: Para cartões de visita e flyers de alta qualidade
    ('Lona Fosca 440g', 'METRO_QUADRADO'),               -- ID 2: Para banners e faixas
    ('Adesivo Vinil Branco', 'METRO_QUADRADO'),          -- ID 3: Para adesivos em geral
    ('Adesivo BOPP Transparente', 'METRO_QUADRADO'),    -- ID 4: Para rótulos resistentes à água
    ('Papel Kraft 180g', 'UNIDADE');                      -- ID 5: Para etiquetas e cartões com visual rústico

-- ETAPA B: LOTES FÍSICOS NO ESTOQUE
INSERT INTO lotes_materia_prima (tipo_materia_prima_id, unidade_de_estoque, atributos, lote_de_origem_id) VALUES
    (1, 'UNIDADE', '{ "formato": "SRA3 (320x450mm)" }', null), -- Lote ID 1
    (2, 'METRO_QUADRADO', '{ "larguraMetros": 1.6 }', null),   -- Lote ID 2
    (3, 'METRO_QUADRADO', '{ "larguraMetros": 1.2 }', null),   -- Lote ID 3
    (4, 'METRO_QUADRADO', '{ "larguraMetros": 1.0 }', null),   -- Lote ID 4
    (5, 'UNIDADE', '{ "formato": "A4 (210x297mm)" }', null);      -- Lote ID 5

-- ETAPA C: REGISTRAR AS ENTRADAS DE ESTOQUE DE INSUMOS
INSERT INTO movimentacoes_estoque_lote (lote_id, data, tipo, quantidade, motivo, custo_por_unidade_base) VALUES
    (1, NOW() - INTERVAL '15 day', 'ENTRADA_COMPRA', 500, 'Nota Fiscal #2024-A1', 1.25), -- 500 folhas SRA3
    (2, NOW() - INTERVAL '10 day', 'ENTRADA_COMPRA', 50, 'Nota Fiscal #2024-B2', 22.50), -- Rolo de 50m² de lona
    (3, NOW() - INTERVAL '5 day', 'ENTRADA_COMPRA', 100, 'Nota Fiscal #2024-C3', 18.00), -- Rolo de 100m² de vinil
    (4, NOW() - INTERVAL '2 day', 'ENTRADA_COMPRA', 30, 'Nota Fiscal #2024-D4', 35.00);  -- Rolo de 30m² de BOPP

-- ETAPA D: CANAIS DE VENDA
INSERT INTO canais_venda (nome) VALUES
    ('Loja Física'),        -- ID 1
    ('Shopee'),             -- ID 2
    ('Site Próprio'),       -- ID 3
    ('Mercado Livre'),      -- ID 4
    ('Equipe de Vendas');   -- ID 5

-- ETAPA E: PRODUTOS ACABADOS DIVERSIFICADOS
INSERT INTO produtos (nome, sku, descricao, cor, unidades_por_produto, ativo, foto_principal_url, tipo_materia_prima_id, largura_cm_unitaria, comprimento_cm_unitario) VALUES
    ('Cartão de Visita Premium', 'CV-PREM-9X5', 'Cartão de visita em papel couchê 300g, laminação fosca.', 'Branco', 100, true, 'cv_premium.jpg', 1, 9.0, 5.0),
    ('Banner Comercial 1,20x0,80m', 'BNR-COM-120X80', 'Banner em lona fosca 440g com bastão e corda.', 'Personalizada', 1, true, 'banner_comercial.jpg', 2, 80.0, 120.0),
    ('Adesivo Redondo 5cm', 'ADSV-RD-5', 'Adesivo em vinil branco para uso geral, corte redondo.', 'Branco', 100, true, 'adesivo_redondo.jpg', 3, 5.0, 5.0),
    ('Folder A4 (Dobrado)', 'FLD-A4-OLD', 'Folder promocional antigo. Produto descontinuado.', 'Colorido', 1, false, 'folder_antigo.jpg', 1, 21.0, 29.7), -- PRODUTO INATIVO
    ('Rótulo para Cerveja Long Neck', 'ROT-CERV-LN', 'Rótulo para garrafas, resistente à umidade, em BOPP transparente.', 'Transparente', 50, true, 'rotulo_cerveja.jpg', 4, 8.0, 7.0),
    ('Adesivo Holográfico (Novo)', 'ADSV-HOLO-10', 'Adesivo com efeito holográfico, corte especial.', 'Holográfico', 100, true, null, 3, 10.0, 10.0), -- PRODUTO NOVO, SEM ESTOQUE
    ('Tag para Roupas Kraft', 'TAG-KFT-4X9', 'Tag de papel kraft 180g com furo.', 'Pardo', 100, true, 'tag_kraft.jpg', 5, 4.0, 9.0);

-- ETAPA F: ESTOQUE MESTRE INICIAL (COM VARIAÇÕES)
INSERT INTO movimentacoes_estoque_produto (produto_id, data, tipo, quantidade, motivo) VALUES
    (1, NOW() - INTERVAL '5 day', 'ENTRADA_PRODUCAO', 5000, 'Ordem de Produção #P101'), -- 5000 cartões
    (2, NOW() - INTERVAL '4 day', 'ENTRADA_PRODUCAO', 10, 'Ordem de Produção #P102'),   -- 10 banners
    (3, NOW() - INTERVAL '3 day', 'ENTRADA_PRODUCAO', 1000, 'Ordem de Produção #P103'), -- 1000 adesivos
    (5, NOW() - INTERVAL '2 day', 'ENTRADA_PRODUCAO', 250, 'Ordem de Produção #P104'),  -- 250 rótulos
    (7, NOW() - INTERVAL '1 day', 'ENTRADA_PRODUCAO', 500, 'Ordem de Produção #P105');   -- 500 tags
-- Note: Produto 4 (inativo) e 6 (novo) não têm estoque inicial.

-- ETAPA G: ESTOQUE DISTRIBUÍDO (COM ESTRATÉGIAS DIFERENTES)
-- Cartões de visita (ID 1) são vendidos principalmente online e na loja.
INSERT INTO estoques (produto_id, canal_venda_id, quantidade) VALUES
    (1, 3, 4000), -- 4000 no Site Próprio
    (1, 1, 1000); -- 1000 na Loja Física

-- Banners (ID 2) são vendidos apenas pela equipe de vendas.
INSERT INTO estoques (produto_id, canal_venda_id, quantidade) VALUES
    (2, 5, 10);

-- Adesivos (ID 3) são vendidos em todos os marketplaces.
INSERT INTO estoques (produto_id, canal_venda_id, quantidade) VALUES
    (3, 2, 500), -- 500 na Shopee
    (3, 4, 500); -- 500 no Mercado Livre

-- Rótulos (ID 5) e Tags (ID 7) têm estoque apenas no site.
INSERT INTO estoques (produto_id, canal_venda_id, quantidade) VALUES
    (5, 3, 250),
    (7, 3, 500);

-- ETAPA H: PREÇOS (PARA PRODUTOS ATIVOS E COM ESTOQUE)
INSERT INTO precos (produto_id, tipo_preco, valor, valor_promocional, promocao_ativa) VALUES
    (1, 'VAREJO', 120.00, 99.90, true), -- Cartão de visita (em promoção!)
    (2, 'VAREJO', 85.00, null, false),   -- Banner
    (3, 'VAREJO', 45.00, null, false),   -- Adesivo
    (5, 'VAREJO', 60.00, null, false),   -- Rótulo
    (7, 'VAREJO', 30.00, null, false);   -- Tag

-- ETAPA I: VENDAS DE EXEMPLO
-- Venda 1: Site Próprio, cliente comprou cartões na promoção.
INSERT INTO sales (sale_date, canal_venda_id, total_amount) VALUES (NOW() - INTERVAL '1 day', 3, 99.90);
INSERT INTO sale_items (sale_id, produto_id, quantity, unit_price, total_price) VALUES (1, 1, 1, 99.90, 99.90);

-- Venda 2: Equipe de Vendas fechou um pedido de 2 banners.
INSERT INTO sales (sale_date, canal_venda_id, total_amount) VALUES (NOW() - INTERVAL '12 hour', 5, 170.00);
INSERT INTO sale_items (sale_id, produto_id, quantity, unit_price, total_price) VALUES (2, 2, 2, 85.00, 170.00);

-- Venda 3: Cliente comprou adesivos e tags na Shopee (usando estoque do site, hipoteticamente)
INSERT INTO sales (sale_date, canal_venda_id, total_amount) VALUES (NOW(), 2, 75.00);
INSERT INTO sale_items (sale_id, produto_id, quantity, unit_price, total_price) VALUES
    (3, 3, 1, 45.00, 45.00),
    (3, 7, 1, 30.00, 30.00);

-- RESPONSABILIDADE: Limpar e popular o banco com um conjunto de dados de desenvolvimento rico e realista.
-- Sendo uma migração REPETÍVEL (R__), o Flyway a executará sempre que o seu conteúdo for alterado.

-- 1. LIMPEZA COMPLETA DAS TABELAS (EM ORDEM DE DEPENDÊNCIA)
TRUNCATE TABLE
    sales,
    sale_items,
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
ALTER SEQUENCE sales_id_seq RESTART WITH 1;
ALTER SEQUENCE sale_items_id_seq RESTART WITH 1;

-- 3. INSERÇÃO DE DADOS DE DESENVOLVIMENTO

-- ETAPA A: INSERIR TIPOS DE MATÉRIAS-PRIMAS (O CATÁLOGO)
INSERT INTO tipos_materia_prima (nome, unidade_de_consumo) VALUES
                                                               ('Adesivo Kraft Pardo', 'CENTIMETRO_QUADRADO'),
                                                               ('Adesivo Vinil Branco Brilho', 'CENTIMETRO_QUADRADO'),
                                                               ('Adesivo Bopp Transparente', 'CENTIMETRO_QUADRADO'),
                                                               ('Tinta Preta para Impressão', 'MILILITRO'),
                                                               ('Lâmina de Corte 45°', 'UNIDADE'),
                                                               ('Papel Couchê 250g', 'FOLHA'),
                                                               ('Tinta Ciano para Impressão', 'MILILITRO');

-- ETAPA B: INSERIR LOTES FÍSICOS NO ESTOQUE
INSERT INTO lotes_materia_prima (tipo_materia_prima_id, unidade_de_estoque, atributos, lote_de_origem_id) VALUES
                                                                                                              (1, 'METRO_LINEAR', '{ "larguraMm": 300, "fornecedor": "Papelaria Central" }', null),
                                                                                                              (1, 'METRO_LINEAR', '{ "larguraMm": 500, "fornecedor": "Importados SA" }', null),
                                                                                                              (2, 'METRO_LINEAR', '{ "larguraMm": 500, "fornecedor": "Adesivos Sul" }', null),
                                                                                                              (4, 'LITRO', '{ "marca": "InkMaster", "validade": "2026-12-31" }', null),
                                                                                                              (5, 'UNIDADE', '{ "marca": "CutPro", "compatibilidade": "Plotter-X2" }', null),
                                                                                                              (6, 'UNIDADE', '{ "formato": "A4", "gramatura": 250 }', null),
                                                                                                              (7, 'LITRO', '{ "marca": "ColorJet", "validade": "2027-01-15" }', null);

-- ETAPA C: REGISTAR AS ENTRADAS DE ESTOQUE DE INSUMOS (AGORA COM CUSTO)
INSERT INTO movimentacoes_estoque_lote (lote_id, data, tipo, quantidade, motivo, custo_por_unidade_base) VALUES
                                                                                                             (1, NOW(), 'ENTRADA_COMPRA', 50.00, 'Nota Fiscal #1001', 0.000667),
                                                                                                             (2, NOW(), 'ENTRADA_COMPRA', 25.00, 'Nota Fiscal #1002', 0.000720),
                                                                                                             (3, NOW(), 'ENTRADA_COMPRA', 100.00, 'Nota Fiscal #1003', 0.000500),
                                                                                                             (4, NOW(), 'ENTRADA_COMPRA', 5.00, 'Nota Fiscal #2001 - 5 Litros', 0.06),
                                                                                                             (5, NOW(), 'ENTRADA_COMPRA', 10.00, 'Nota Fiscal #3001 - Caixa com 10 lâminas', 12.00),
                                                                                                             (6, NOW(), 'ENTRADA_COMPRA', 500.00, 'Nota Fiscal #4001 - Resma A4', 0.15),
                                                                                                             (7, NOW(), 'ENTRADA_COMPRA', 1.00, 'Nota Fiscal #2002 - 1 Litro', 0.08);

-- ETAPA D: INSERIR CANAIS DE VENDA
INSERT INTO canais_venda (nome) VALUES
                                    ('LOJA_FISICA'), ('SHOPEE'), ('MERCADO_LIVRE'), ('WHATSAPP'), ('OUTROS');

-- ETAPA E: INSERIR PRODUTOS ACABADOS
INSERT INTO produtos (nome, sku, descricao, cor, unidades_por_produto, ativo, foto_principal_url) VALUES
                                                                                                      ('Etiqueta Redonda Kraft 5x5cm', 'ETQ-KFT-RD-50', 'Pacote com 100 etiquetas redondas em papel kraft pardo.', 'Pardo', 100, true, 'https://placehold.co/600x400/D2B48C/FFFFFF?text=Etiqueta+Kraft'),
                                                                                                      ('Etiqueta Retangular Vinil 9x5cm', 'ETQ-VNL-RT-95', 'Pacote com 100 etiquetas em vinil branco a prova d''água.', 'Branco', 100, true, 'https://placehold.co/600x400/FFFFFF/000000?text=Etiqueta+Vinil'),
                                                                                                      ('Cartão de Visita Couchê 9x5cm', 'CARD-COU-95', '1000 cartões de visita com impressão colorida frente e verso.', 'Colorido', 1000, true, 'https://placehold.co/600x400/E9E9E9/333333?text=Cartão+Visita');

-- ETAPA F: MONTAR A "RECEITA" (COMPOSIÇÃO) DOS PRODUTOS
INSERT INTO composicao_produto (produto_id, tipo_materia_prima_id, gasto_material_por_unidade) VALUES
                                                                                                   (1, 1, 25.0000), -- Etiqueta Kraft usa Adesivo Kraft
                                                                                                   (2, 2, 45.0000), -- Etiqueta Vinil usa Adesivo Vinil
                                                                                                   (3, 6, 1.0000),  -- Cartão de Visita usa 1 Folha de Papel Couchê
                                                                                                   (3, 4, 0.5000),  -- ... e consome 0.5ml de Tinta Preta
                                                                                                   (3, 7, 0.5000);  -- ... e consome 0.5ml de Tinta Ciano

-- ETAPA G: DEFINIR O ESTOQUE MESTRE INICIAL
INSERT INTO movimentacoes_estoque_produto (produto_id, data, tipo, quantidade, motivo) VALUES
                                                                                           (1, NOW(), 'AJUSTE_MANUAL', 60, 'Carga inicial de estoque'),
                                                                                           (2, NOW(), 'AJUSTE_MANUAL', 50, 'Carga inicial de estoque'),
                                                                                           (3, NOW(), 'ENTRADA_PRODUCAO', 10, 'Produção interna');

-- ETAPA H: DEFINIR O ESTOQUE DISTRIBUÍDO
INSERT INTO estoques (produto_id, canal_venda_id, quantidade) VALUES
                                                                  (1, 1, 10), (1, 2, 50),
                                                                  (2, 1, 20), (2, 3, 30),
                                                                  (3, 1, 10);

-- ETAPA I: DEFINIR OS PREÇOS DOS PRODUTOS
INSERT INTO precos (produto_id, tipo_preco, valor, valor_promocional, promocao_ativa) VALUES
                                                                                          (1, 'VAREJO', 25.00, 19.90, true),
                                                                                          (1, 'REVENDA', 18.50, null, false),
                                                                                          (2, 'VAREJO', 35.00, null, false),
                                                                                          (2, 'REVENDA', 28.00, null, false),
                                                                                          (3, 'VAREJO', 99.90, 89.90, true),
                                                                                          (3, 'REVENDA', 75.00, null, false);
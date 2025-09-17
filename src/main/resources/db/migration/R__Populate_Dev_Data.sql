-- RESPONSABILIDADE: Limpar e popular o banco com dados de desenvolvimento.
-- Sendo uma migração REPETÍVEL (R__), o Flyway a executará sempre que o seu conteúdo for alterado.

-- 1. LIMPEZA COMPLETA DAS TABELAS (EM ORDEM DE DEPENDÊNCIA)
TRUNCATE TABLE estoques, precos, composicao_produto, movimentacoes_estoque_lote, lotes_materia_prima, tipos_materia_prima, produtos, canais_venda CASCADE;

-- 2. RESET DAS SEQUÊNCIAS DE ID PARA PREVISIBILIDADE NOS TESTES
ALTER SEQUENCE tipos_materia_prima_id_seq RESTART WITH 1;
ALTER SEQUENCE lotes_materia_prima_id_seq RESTART WITH 1;
ALTER SEQUENCE movimentacoes_estoque_lote_id_seq RESTART WITH 1;
ALTER SEQUENCE produtos_id_seq RESTART WITH 1;
ALTER SEQUENCE composicao_produto_id_seq RESTART WITH 1;
ALTER SEQUENCE precos_id_seq RESTART WITH 1;
ALTER SEQUENCE canais_venda_id_seq RESTART WITH 1;
ALTER SEQUENCE estoques_id_seq RESTART WITH 1;

-- 3. INSERÇÃO DE DADOS DE DESENVOLVIMENTO

-- ETAPA A: INSERIR TIPOS DE MATÉRIAS-PRIMAS (O CATÁLOGO)
    INSERT INTO tipos_materia_prima (nome, unidade_de_consumo) VALUES
                                                               ('Adesivo Kraft Pardo', 'CENTIMETRO_QUADRADO'),
                                                               ('Adesivo Vinil Branco Brilho', 'CENTIMETRO_QUADRADO'),
                                                               ('Adesivo Bopp Transparente', 'CENTIMETRO_QUADRADO'),
                                                               ('Tinta Preta para Impressão', 'MILILITRO'),
                                                               ('Lâmina de Corte 45°', 'UNIDADE');

-- ETAPA B: INSERIR LOTES FÍSICOS NO ESTOQUE (O QUE VOCÊ TEM)
INSERT INTO lotes_materia_prima (tipo_materia_prima_id, unidade_de_estoque, atributos) VALUES
                                                                                           (1, 'METRO_LINEAR', '{ "larguraMm": 300, "fornecedor": "Papelaria Central" }'),
                                                                                           (1, 'METRO_LINEAR', '{ "larguraMm": 500, "fornecedor": "Importados SA" }'),
                                                                                           (2, 'METRO_LINEAR', '{ "larguraMm": 500, "fornecedor": "Adesivos Sul" }'),
                                                                                           (4, 'LITRO', '{ "marca": "InkMaster", "validade": "2026-12-31" }'),
                                                                                           (5, 'UNIDADE', '{ "marca": "CutPro", "compatibilidade": "Plotter-X2" }');

-- ETAPA C: REGISTRAR AS ENTRADAS DE ESTOQUE INICIAIS (O "LIVRO-RAZÃO")
INSERT INTO movimentacoes_estoque_lote (lote_id, data, tipo, quantidade, motivo) VALUES
                                                                                     (1, NOW(), 'ENTRADA_COMPRA', 50.00, 'Nota Fiscal #1001'),
                                                                                     (2, NOW(), 'ENTRADA_COMPRA', 25.00, 'Nota Fiscal #1002'),
                                                                                     (3, NOW(), 'ENTRADA_COMPRA', 100.00, 'Nota Fiscal #1003'),
                                                                                     (4, NOW(), 'ENTRADA_COMPRA', 5.00, 'Nota Fiscal #2001 - 5 Litros'),
                                                                                     (5, NOW(), 'ENTRADA_COMPRA', 10.00, 'Nota Fiscal #3001 - Caixa com 10 lâminas');

-- ETAPA D: INSERIR CANAIS DE VENDA
INSERT INTO canais_venda (nome) VALUES
                                    ('LOJA_FISICA'), ('SHOPEE'), ('MERCADO_LIVRE'), ('WHATSAPP'), ('OUTROS');

-- ETAPA E: INSERIR PRODUTOS ACABADOS
INSERT INTO produtos (nome, sku, descricao, cor, unidades_por_produto, ativo, foto_principal_url) VALUES
                                                                                                      ('Etiqueta Redonda Kraft 5x5cm', 'ETQ-KFT-RD-50', 'Pacote com 100 etiquetas redondas em papel kraft pardo, ideal para personalização.', 'Pardo', 100, true, 'https://placehold.co/600x400/D2B48C/FFFFFF?text=Etiqueta+Kraft'),
                                                                                                      ('Etiqueta Retangular Vinil 9x5cm', 'ETQ-VNL-RT-95', 'Pacote com 100 etiquetas em vinil branco a prova d''água.', 'Branco', 100, true, 'https://placehold.co/600x400/FFFFFF/000000?text=Etiqueta+Vinil');

-- ETAPA F: MONTAR A "RECEITA" (COMPOSIÇÃO) DOS PRODUTOS
INSERT INTO composicao_produto (produto_id, tipo_materia_prima_id, gasto_material_por_unidade) VALUES
                                                                                                   (1, 1, 25.0000), -- Etiqueta Kraft (5x5cm = 25cm²) usa o TIPO 1 (Adesivo Kraft)
                                                                                                   (2, 2, 45.0000); -- Etiqueta Vinil (9x5cm = 45cm²) usa o TIPO 2 (Adesivo Vinil)

-- ETAPA G: DEFINIR OS PREÇOS DOS PRODUTOS
INSERT INTO precos (produto_id, tipo_preco, valor) VALUES
                                                       (1, 'VAREJO', 25.00), (1, 'REVENDA', 18.50),
                                                       (2, 'VAREJO', 35.00), (2, 'REVENDA', 28.00);

-- ETAPA H: DEFINIR O ESTOQUE INICIAL DE PRODUTOS ACABADOS
INSERT INTO estoques (produto_id, canal_venda_id, quantidade) VALUES
                                                                  (1, 1, 10), (1, 2, 50),
                                                                  (2, 1, 20), (2, 3, 30);


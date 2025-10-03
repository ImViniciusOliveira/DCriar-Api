-- RESPONSABILIDADE: Limpar e popular o banco com um conjunto de dados de desenvolvimento rico e realista.
-- Sendo uma migração REPETÍVEL (R__), o Flyway a executará sempre que o seu conteúdo for alterado.

-- 1. LIMPEZA COMPLETA DAS TABELAS (EM ORDEM DE DEPENDÊNCIA)
TRUNCATE TABLE
    sales,
    sale_items,
    estoques,
    precos,
    cortes_realizados,
    ordens_de_corte,
    movimentacoes_estoque_produto,
    movimentacoes_estoque_lote,
    lotes_materia_prima,
    tipos_materia_prima,
    produtos,
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
                                                               ('Adesivo Vinil Branco Brilho', 'CENTIMETRO_QUADRADO');

-- ETAPA B: INSERIR LOTES FÍSICOS NO ESTOQUE
INSERT INTO lotes_materia_prima (tipo_materia_prima_id, unidade_de_estoque, atributos, lote_de_origem_id) VALUES
                                                                                                              (1, 'METRO_LINEAR', '{ "larguraMm": 300 }', null),
                                                                                                              (2, 'METRO_LINEAR', '{ "larguraMm": 500 }', null);

-- ETAPA C: REGISTRAR AS ENTRADAS DE ESTOQUE DE INSUMOS
INSERT INTO movimentacoes_estoque_lote (lote_id, data, tipo, quantidade, motivo, custo_por_unidade_base) VALUES
                                                                                                             (1, NOW(), 'ENTRADA_COMPRA', 50.00, 'Nota Fiscal #1001', 0.000667),
                                                                                                             (2, NOW(), 'ENTRADA_COMPRA', 100.00, 'Nota Fiscal #1003', 0.000500);

-- ETAPA D: INSERIR CANAIS DE VENDA
INSERT INTO canais_venda (nome) VALUES ('LOJA_FISICA'), ('SHOPEE');

-- ETAPA E: INSERIR PRODUTOS ACABADOS (AGORA COMO "MOLDES")
INSERT INTO produtos (nome, sku, descricao, cor, unidades_por_produto, ativo, tipo_materia_prima_id, largura_cm_unitaria, comprimento_cm_unitario) VALUES
                                                                                                                                                       ('Etiqueta Redonda Kraft 5x5cm', 'ETQ-KFT-RD-50', 'Pacote com 100 etiquetas.', 'Pardo', 100, true, 1, 5.0, 5.0),
                                                                                                                                                       ('Etiqueta Retangular Vinil 9x5cm', 'ETQ-VNL-RT-95', 'Pacote com 100 etiquetas.', 'Branco', 100, true, 2, 9.0, 5.0);

-- ETAPA F: DEFINIR O ESTOQUE MESTRE INICIAL
INSERT INTO movimentacoes_estoque_produto (produto_id, data, tipo, quantidade, motivo) VALUES
                                                                                           (1, NOW(), 'AJUSTE_MANUAL', 60, 'Carga inicial de estoque'),
                                                                                           (2, NOW(), 'AJUSTE_MANUAL', 50, 'Carga inicial de estoque');

-- ETAPA G: DEFINIR O ESTOQUE DISTRIBUÍDO
INSERT INTO estoques (produto_id, canal_venda_id, quantidade) VALUES
                                                                  (1, 1, 10), (1, 2, 50),
                                                                  (2, 1, 20), (2, 2, 0);

-- ETAPA H: DEFINIR OS PREÇOS
INSERT INTO precos (produto_id, tipo_preco, valor) VALUES
                                                       (1, 'VAREJO', 25.00),
                                                       (2, 'VAREJO', 35.00);

-- ETAPA I: INSERIR ORDENS DE CORTE DE EXEMPLO (COM LÓGICA CORRIGIDA)
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
                                                                                                    (2, 1.0, 260.0, 1, 'SOBRA');
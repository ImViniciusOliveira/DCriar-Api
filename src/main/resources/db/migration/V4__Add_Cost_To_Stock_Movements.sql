-- RESPONSABILIDADE: Evoluir o schema, adicionando a coluna de custo à tabela de movimentações de lote.

ALTER TABLE movimentacoes_estoque_lote
ADD COLUMN custo_por_unidade_base NUMERIC(19, 8);

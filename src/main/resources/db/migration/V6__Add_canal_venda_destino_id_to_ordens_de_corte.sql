-- MIGRAÇÃO V6: Adiciona o campo canal_venda_destino_id à tabela ordens_de_corte
-- Esse campo permite registrar para qual canal de venda o produto cortado será destinado,
-- facilitando o rastreamento e a gestão de ordens de corte por canal de venda.
ALTER TABLE ordens_de_corte
ADD COLUMN canal_venda_destino_id BIGINT;
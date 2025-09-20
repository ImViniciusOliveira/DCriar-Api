package com.dcriar.domain.sales.service.impl;

import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.request.sales.SaleRequestDTO;
import com.dcriar.api.dto.request.sales.SaleItemRequestDTO;
import com.dcriar.api.dto.response.sales.SaleResponseDTO;
import com.dcriar.api.mapper.sales.SaleMapper;
import com.dcriar.domain.product.entity.CanalVenda;
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
import com.dcriar.domain.product.entity.Preco;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.entity.enuns.TipoMovimentacaoProduto;
import com.dcriar.domain.product.entity.enuns.TipoPreco;
import com.dcriar.domain.product.repository.CanalVendaRepository;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.PrecoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.product.service.EstoqueProdutoService;
import com.dcriar.domain.sales.entity.Sale;
import com.dcriar.domain.sales.entity.SaleItem;
import com.dcriar.domain.sales.repository.SaleRepository;
import com.dcriar.domain.sales.service.SaleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para o Módulo de Vendas.
 */
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProdutoRepository produtoRepository;
    private final CanalVendaRepository canalVendaRepository;
    private final PrecoRepository precoRepository;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final EstoqueProdutoService estoqueProdutoService;
    private final SaleMapper saleMapper;

    @Override
    @Transactional
    public SaleResponseDTO registerSale(SaleRequestDTO requestDTO) {
        CanalVenda canalVenda = canalVendaRepository.findById(requestDTO.getCanalVendaId())
                .orElseThrow(() -> new EntityNotFoundException("Canal de Venda não encontrado com o ID: " + requestDTO.getCanalVendaId()));

        Sale newSale = Sale.builder()
                .canalVenda(canalVenda)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1. Processa cada item do "carrinho"
        for (SaleItemRequestDTO itemDTO : requestDTO.getItems()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + itemDTO.getProdutoId()));

            Preco preco = precoRepository.findByProdutoAndTipoPreco(produto, TipoPreco.VAREJO)
                    .orElseThrow(() -> new IllegalStateException("Preço de Varejo não definido para o produto: " + produto.getNome()));

            BigDecimal unitPrice = preco.isPromocaoAtiva() && preco.getValorPromocional() != null ? preco.getValorPromocional() : preco.getValor();
            BigDecimal itemTotalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            SaleItem saleItem = SaleItem.builder()
                    .produto(produto)
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotalPrice)
                    .build();

            newSale.addItem(saleItem);
            totalAmount = totalAmount.add(itemTotalPrice);

            // 2. Orquestra a "Baixa Dupla" no estoque
            performStockReduction(produto, canalVenda, itemDTO.getQuantity(), newSale.getId());
        }

        newSale.setTotalAmount(totalAmount);
        Sale savedSale = saleRepository.save(newSale);

        return saleMapper.toResponseDTO(savedSale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDTO> findAll() {
        return saleRepository.findAll().stream()
                .map(saleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponseDTO findById(Long id) {
        return saleRepository.findById(id)
                .map(saleMapper::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada com o ID: " + id));
    }

    private void performStockReduction(Produto produto, CanalVenda canalVenda, int quantity, Long saleId) {
        // a. Baixa no Estoque do Canal
        AjusteEstoqueRequestDTO ajusteDTO = AjusteEstoqueRequestDTO.builder()
                .produtoId(produto.getId())
                .canalVendaId(canalVenda.getId())
                .quantidade(quantity * -1) // Quantidade negativa para remover
                .build();
        estoqueProdutoService.ajustarEstoque(ajusteDTO);

        // b. Baixa no "Estoque Mestre" Físico
        MovimentacaoEstoqueProduto movimentacaoVenda = MovimentacaoEstoqueProduto.builder()
                .produto(produto)
                .tipo(TipoMovimentacaoProduto.SAIDA_VENDA)
                .quantidade(quantity * -1)
                // A lógica para o motivo foi melhorada para lidar com o ID nulo antes de salvar.
                .motivo("Venda no canal: " + canalVenda.getNome())
                .build();
        movimentacaoEstoqueProdutoRepository.save(movimentacaoVenda);
    }
}


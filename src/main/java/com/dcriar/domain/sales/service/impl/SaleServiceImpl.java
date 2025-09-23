package com.dcriar.domain.sales.service.impl;

import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.request.sales.SaleItemRequestDTO;
import com.dcriar.api.dto.request.sales.SaleRequestDTO;
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
import com.dcriar.exception.custom.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação da lógica de negócio para o módulo de Vendas (Sales).
 * Observações importantes:
 * - O método ajustarEstoque do EstoqueProdutoService lança {@link EstoqueInsuficienteException}
 *   quando não há saldo suficiente. Aqui nós deixamos essa exceção propagar (não a "transformamos")
 *   para que o {@link com.dcriar.exception.handler.GlobalExceptionHandler} construa um ErrorDTO
 *   com detalhes (produtoId, canalVendaId, quantidadeRequisitada, estoqueAtual).
 * - O método é transacional: se qualquer item falhar (ex.: estoque insuficiente), toda a operação
 *   será revertida.
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
                .orElseThrow(() -> new CanalVendaNotFoundException(requestDTO.getCanalVendaId()));

        // Cria a venda em memória (não precisa persistir antes das baixas; a transação garante rollback em caso de erro)
        Sale newSale = Sale.builder()
                .canalVenda(canalVenda)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Processa cada item: valida produto, calcula preço, faz baixa de estoque e adiciona item à venda
        for (SaleItemRequestDTO itemDTO : requestDTO.getItems()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new ProdutoNotFoundException(itemDTO.getProdutoId()));

            Preco preco = precoRepository.findByProdutoAndTipoPreco(produto, TipoPreco.VAREJO)
                    .orElseThrow(() -> new PrecoVarejoNaoDefinidoException(produto.getId()));

            BigDecimal unitPrice = preco.isPromocaoAtiva() && preco.getValorPromocional() != null
                    ? preco.getValorPromocional()
                    : preco.getValor();

            BigDecimal itemTotalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            SaleItem saleItem = SaleItem.builder()
                    .produto(produto)
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotalPrice)
                    .build();

            newSale.addItem(saleItem);
            totalAmount = totalAmount.add(itemTotalPrice);

            // Faz a redução de estoque no canal e no estoque mestre.
            // OBS: ajustarEstoque pode lançar EstoqueInsuficienteException com contexto completo.
            performStockReduction(produto, canalVenda, itemDTO.getQuantity());
        }

        newSale.setTotalAmount(totalAmount);
        Sale savedSale = saleRepository.save(newSale);

        // Observação: se for interessante registrar o saleId no motivo das movimentações,
        // podemos salvar a venda antes de processar as baixas (ou atualizar as movimentações depois).
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
                .orElseThrow(() -> new SaleNotFoundException(id));
    }

    /**
     * Realiza as baixas de estoque no canal e cria a movimentação no estoque mestre.
     * Importante:
     * - deixamos a {@link EstoqueInsuficienteException} propagar para que o GlobalExceptionHandler
     *   crie uma resposta http/400 com detalhes (produtoId, canalVendaId, quantidadeRequisitada, estoqueAtual).
     *
     * @param produto produto que será baixado
     * @param canalVenda canal onde será feita a baixa
     * @param quantity quantidade a remover (valor positivo aqui representa unidades vendidas)
     */
    private void performStockReduction(Produto produto, CanalVenda canalVenda, int quantity) {
        // Ajusta o estoque no canal (quantidade negativa: saída)
        AjusteEstoqueRequestDTO ajusteDTO = AjusteEstoqueRequestDTO.builder()
                .produtoId(produto.getId())
                .canalVendaId(canalVenda.getId())
                .quantidade(quantity * -1)
                .build();

        // Chamada que pode lançar EstoqueInsuficienteException (com estoqueAtual etc.). Não capturamos nem transformamos.
        estoqueProdutoService.ajustarEstoque(ajusteDTO);

        // Depois da baixa no canal, registra a movimentação no estoque mestre (saída de venda)
        MovimentacaoEstoqueProduto movimentacaoVenda = MovimentacaoEstoqueProduto.builder()
                .produto(produto)
                .tipo(TipoMovimentacaoProduto.SAIDA_VENDA)
                .quantidade(quantity * -1)
                .motivo("Venda no canal: " + canalVenda.getNome())
                .build();

        movimentacaoEstoqueProdutoRepository.save(movimentacaoVenda);
    }
}

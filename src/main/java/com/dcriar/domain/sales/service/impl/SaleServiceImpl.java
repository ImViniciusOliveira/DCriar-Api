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
import com.dcriar.domain.product.entity.enums.TipoMovimentacaoProduto;
import com.dcriar.domain.product.entity.enums.TipoPreco;
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
 * <p>
 * Esta classe é responsável por orquestrar o processo de registro de vendas,
 * incluindo a validação, cálculo de preços, ajuste de estoque e persistência dos dados.
 * <p>
 * Observações importantes:
 * <ul>
 *     <li>O método {@code ajustarEstoque} do {@code EstoqueProdutoService} lança
 *     {@link EstoqueInsuficienteCanalException} quando não há saldo suficiente.
 *     Deixamos essa exceção propagar para que o {@link com.dcriar.exception.handler.GlobalExceptionHandler}
 *     construa um {@code ErrorDTO} com detalhes (produtoId, canalVendaId, quantidadeRequisitada, estoqueAtual).</li>
 *     <li>Todos os métodos que modificam o estado do banco de dados são transacionais,
 *     garantindo que, se qualquer item falhar (ex.: estoque insuficiente), toda a operação
 *     será revertida (rollback).</li>
 * </ul>
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

    /**
     * Regista uma nova venda no sistema e orquestra a baixa automática de estoque.
     * <p>
     * O processo envolve:
     * <ol>
     *     <li>Busca e validação do canal de venda.</li>
     *     <li>Criação de uma nova entidade de venda em memória.</li>
     *     <li>Iteração sobre cada item da requisição:
     *         <ul>
     *             <li>Busca e validação do produto.</li>
     *             <li>Determinação do preço unitário (considerando promoções).</li>
     *             <li>Criação e adição do item à venda.</li>
     *             <li>Realização da redução de estoque no canal e no estoque mestre.</li>
     *         </ul>
     *     </li>
     *     <li>Cálculo do valor total da venda.</li>
     *     <li>Persistência da venda e seus itens.</li>
     * </ol>
     *
     * @param requestDTO O DTO contendo os dados da nova venda.
     * @return Um {@link SaleResponseDTO} representando a venda registrada.
     * @throws CanalVendaNotFoundException se o canal de venda não for encontrado.
     * @throws ProdutoNotFoundException se um produto de um item não for encontrado.
     * @throws PrecoVarejoNaoDefinidoException se o preço de varejo para um produto não estiver definido.
     * @throws EstoqueInsuficienteCanalException se não houver estoque suficiente no canal para um produto.
     */
    @Override
    @Transactional
    public SaleResponseDTO registerSale(SaleRequestDTO requestDTO) {
        CanalVenda canalVenda = canalVendaRepository.findById(requestDTO.getCanalVendaId())
                .orElseThrow(() -> new CanalVendaNotFoundException(requestDTO.getCanalVendaId()));

        Sale newSale = Sale.builder()
                .canalVenda(canalVenda)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SaleItemRequestDTO itemDTO : requestDTO.getItems()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new ProdutoNotFoundException(itemDTO.getProdutoId()));

            Preco preco = precoRepository.findByProdutoAndTipoPreco(produto, TipoPreco.VAREJO)
                    .orElseThrow(() -> new PrecoVarejoNaoDefinidoException(produto.getId()));

            BigDecimal unitPrice = preco.isPromocaoAtiva() && preco.getValorPromocional() != null
                    ? preco.getValorPromocional()
                    : preco.getValor();

            BigDecimal itemTotalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantidade()));

            SaleItem saleItem = SaleItem.builder()
                    .produto(produto)
                    .quantity(itemDTO.getQuantidade())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotalPrice)
                    .build();

            newSale.addItem(saleItem);
            totalAmount = totalAmount.add(itemTotalPrice);

            performStockReduction(produto, canalVenda, itemDTO.getQuantidade());
        }

        newSale.setTotalAmount(totalAmount);
        Sale savedSale = saleRepository.save(newSale);

        return saleMapper.toResponseDTO(savedSale);
    }

    /**
     * Lista todas as vendas registradas no sistema.
     *
     * @return Uma lista de {@link SaleResponseDTO} contendo todas as vendas.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDTO> findAll() {
        return saleRepository.findAll().stream()
                .map(saleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma venda específica pelo seu ID.
     *
     * @param id O ID da venda a ser buscada.
     * @return O {@link SaleResponseDTO} da venda encontrada.
     * @throws SaleNotFoundException se a venda com o ID especificado não for encontrada.
     */
    @Override
    @Transactional(readOnly = true)
    public SaleResponseDTO findById(Long id) {
        return saleRepository.findById(id)
                .map(saleMapper::toResponseDTO)
                .orElseThrow(() -> new SaleNotFoundException(id));
    }

    /**
     * Realiza as baixas de estoque no canal e cria a movimentação no estoque mestre.
     * <p>
     * Este método é responsável por:
     * <ol>
     *     <li>Ajustar o estoque do produto no canal de venda (redução).</li>
     *     <li>Registrar uma movimentação de saída no estoque mestre do produto.</li>
     * </ol>
     * Deixamos a {@link EstoqueInsuficienteCanalException} propagar para que o {@code GlobalExceptionHandler}
     * crie uma resposta HTTP 400 com detalhes (produtoId, canalVendaId, quantidadeRequisitada, estoqueAtual).
     *
     * @param produto O produto que terá seu estoque reduzido.
     * @param canalVenda O canal de venda onde a baixa será efetuada.
     * @param quantity A quantidade a ser removida (valor positivo aqui representa unidades vendidas).
     * @throws EstoqueInsuficienteCanalException se não houver estoque suficiente no canal.
     */
    private void performStockReduction(Produto produto, CanalVenda canalVenda, int quantity) {
        AjusteEstoqueRequestDTO ajusteDTO = AjusteEstoqueRequestDTO.builder()
                .produtoId(produto.getId())
                .canalVendaId(canalVenda.getId())
                .quantidade(quantity * -1) // Quantidade negativa para indicar saída
                .build();

        estoqueProdutoService.ajustarEstoque(ajusteDTO);

        MovimentacaoEstoqueProduto movimentacaoVenda = MovimentacaoEstoqueProduto.builder()
                .produto(produto)
                .tipo(TipoMovimentacaoProduto.SAIDA_VENDA)
                .quantidade(quantity * -1)
                .motivo(String.format("Venda no canal: %s", canalVenda.getNome()))
                .build();

        movimentacaoEstoqueProdutoRepository.save(movimentacaoVenda);
    }
}

package com.dcriar.domain.production.service.impl;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.domain.product.entity.Dimensoes;
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.entity.enums.TipoMovimentacaoProduto;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import com.dcriar.domain.stock.entity.MovimentacaoEstoqueLote;
import com.dcriar.domain.stock.entity.enums.TipoMovimentacao;
import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import com.dcriar.domain.production.enums.ModoCalculo;
import com.dcriar.domain.production.service.OrdemDeProducaoService;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.product.service.EstoqueProdutoService;
import com.dcriar.domain.stock.repository.LoteMateriaPrimaRepository;
import com.dcriar.domain.stock.repository.MovimentacaoEstoqueLoteRepository;
import com.dcriar.exception.custom.LoteMateriaPrimaNotFoundException;
import com.dcriar.exception.custom.ProdutoNotFoundException;
import com.dcriar.exception.custom.RegraNegocioException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação da lógica de negócio para Ordens de Produção Híbridas.
 */
@Service
@RequiredArgsConstructor
public class OrdemDeProducaoServiceImpl implements OrdemDeProducaoService {

    private final LoteMateriaPrimaRepository loteMateriaPrimaRepository;
    private final MovimentacaoEstoqueLoteRepository movimentacaoEstoqueLoteRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final EstoqueProdutoService estoqueProdutoService;

    @Override
    @Transactional
    public void processarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO) {
        LoteMateriaPrima lotePrincipal = findLoteById(requestDTO.getLotePrincipalId());
        Produto produto = findProdutoById(requestDTO.getProdutoId());

        // 1. Determina as dimensões finais do corte com base no modo de cálculo
        Dimensoes dimensoesFinais = calcularDimensoesFinais(requestDTO, produto);

        // 2. Validações de Negócio (agora usando as dimensões finais)
        validarOrdemDeCorte(lotePrincipal, dimensoesFinais);

        // 3. Cálculo do consumo e da sobra
        BigDecimal larguraTotalLoteCm = getLarguraEmCm(lotePrincipal.getAtributos());
        BigDecimal comprimentoDeCorteMetros = dimensoesFinais.getComprimento().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal larguraSobraCm = larguraTotalLoteCm.subtract(dimensoesFinais.getLargura());

        // 4. Regista a saída no lote principal
        registrarSaida(lotePrincipal, comprimentoDeCorteMetros, requestDTO.getMotivo());

        // 5. Cria o novo lote de retalho, se houver sobra
        if (larguraSobraCm.compareTo(BigDecimal.ZERO) > 0) {
            criarLoteDeRetalho(lotePrincipal, larguraSobraCm, comprimentoDeCorteMetros);
        }

        // 6. Regista a entrada do produto acabado e distribui para o canal
        registrarEntradaProdutoAcabado(requestDTO, produto);
        distribuirEstoqueParaCanal(requestDTO);
    }

    private Dimensoes calcularDimensoesFinais(OrdemDeCorteRequestDTO requestDTO, Produto produto) {
        if (requestDTO.getModoCalculo() == ModoCalculo.MANUAL) {
            if (requestDTO.getTamanhoFinal() == null || requestDTO.getTamanhoFinal().getLargura() == null || requestDTO.getTamanhoFinal().getComprimento() == null) {
                throw new RegraNegocioException("Para o modo MANUAL, as dimensões finais (largura e comprimento) são obrigatórias.");
            }
            return new Dimensoes(requestDTO.getTamanhoFinal().getLargura(), requestDTO.getTamanhoFinal().getComprimento());
        }

        // Lógica para modo AUTOMATICO
        if (produto.getDimensoesUnitarias() == null) {
            throw new RegraNegocioException("O produto selecionado não possui dimensões unitárias cadastradas para o cálculo automático.");
        }

        BigDecimal larguraUnitaria = produto.getDimensoesUnitarias().getLargura();
        BigDecimal comprimentoUnitario = produto.getDimensoesUnitarias().getComprimento();
        BigDecimal quantidade = new BigDecimal(requestDTO.getQuantidadeProduzida());

        BigDecimal larguraFinal = larguraUnitaria; // Assumindo que a largura não escala com a quantidade
        BigDecimal comprimentoFinal = comprimentoUnitario.multiply(quantidade);

        // Adiciona margens, se fornecidas
        if (requestDTO.getMargens() != null) {
            larguraFinal = larguraFinal
                    .add(Optional.ofNullable(requestDTO.getMargens().getEsquerda()).orElse(BigDecimal.ZERO))
                    .add(Optional.ofNullable(requestDTO.getMargens().getDireita()).orElse(BigDecimal.ZERO));
            comprimentoFinal = comprimentoFinal
                    .add(Optional.ofNullable(requestDTO.getMargens().getSuperior()).orElse(BigDecimal.ZERO))
                    .add(Optional.ofNullable(requestDTO.getMargens().getInferior()).orElse(BigDecimal.ZERO));
        }

        return new Dimensoes(larguraFinal, comprimentoFinal);
    }

    private void validarOrdemDeCorte(LoteMateriaPrima lote, Dimensoes dimensoesFinais) {
        if (lote.getUnidadeDeEstoque() != UnidadeDeMedida.METRO_LINEAR) {
            throw new RegraNegocioException("Ordens de corte só podem ser processadas em lotes com unidade METRO_LINEAR.");
        }

        BigDecimal larguraTotalLoteCm = getLarguraEmCm(lote.getAtributos());
        if (dimensoesFinais.getLargura().compareTo(larguraTotalLoteCm) > 0) {
            throw new RegraNegocioException("A largura de corte final (" + dimensoesFinais.getLargura() + "cm) não pode ser maior que a largura total do lote (" + larguraTotalLoteCm + "cm).");
        }

        BigDecimal saldoAtualMetros = movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
        BigDecimal comprimentoDeCorteMetros = dimensoesFinais.getComprimento().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        if (comprimentoDeCorteMetros.compareTo(saldoAtualMetros) > 0) {
            throw new RegraNegocioException("Saldo de estoque insuficiente. Necessário: " + comprimentoDeCorteMetros + "m, Disponível: " + saldoAtualMetros + "m.");
        }
    }

    private void registrarEntradaProdutoAcabado(OrdemDeCorteRequestDTO requestDTO, Produto produto) {
        MovimentacaoEstoqueProduto entradaProducao = MovimentacaoEstoqueProduto.builder()
                .produto(produto)
                .tipo(TipoMovimentacaoProduto.ENTRADA_PRODUCAO)
                .quantidade(requestDTO.getQuantidadeProduzida())
                .motivo("Produzido via Ordem de Corte. Consumiu o Lote ID: " + requestDTO.getLotePrincipalId())
                .build();
        movimentacaoEstoqueProdutoRepository.save(entradaProducao);
    }

    private void distribuirEstoqueParaCanal(OrdemDeCorteRequestDTO requestDTO) {
        AjusteEstoqueRequestDTO ajusteDTO = AjusteEstoqueRequestDTO.builder()
                .produtoId(requestDTO.getProdutoId())
                .canalVendaId(requestDTO.getCanalVendaDestinoId())
                .quantidade(requestDTO.getQuantidadeProduzida())
                .build();
        estoqueProdutoService.ajustarEstoque(ajusteDTO);
    }

    private void registrarSaida(LoteMateriaPrima lote, BigDecimal comprimentoDeCorteMetros, String motivo) {
        MovimentacaoEstoqueLote saida = MovimentacaoEstoqueLote.builder()
                .lote(lote)
                .tipo(TipoMovimentacao.SAIDA_PRODUCAO)
                .quantidade(comprimentoDeCorteMetros.negate())
                .motivo(motivo)
                .build();
        movimentacaoEstoqueLoteRepository.save(saida);
    }

    private void criarLoteDeRetalho(LoteMateriaPrima lotePrincipal, BigDecimal larguraSobraCm, BigDecimal comprimentoMetros) {
        Map<String, Object> novosAtributos = Map.of("larguraMm", larguraSobraCm.multiply(new BigDecimal("10")).intValue());
        LoteMateriaPrima loteRetalho = LoteMateriaPrima.builder()
                .tipoMateriaPrima(lotePrincipal.getTipoMateriaPrima())
                .unidadeDeEstoque(UnidadeDeMedida.METRO_LINEAR)
                .atributos(novosAtributos)
                .loteDeOrigem(lotePrincipal)
                .build();
        MovimentacaoEstoqueLote entradaRetalho = MovimentacaoEstoqueLote.builder()
                .lote(loteRetalho)
                .tipo(TipoMovimentacao.ENTRADA_SOBRA)
                .quantidade(comprimentoMetros)
                .motivo("Retalho gerado a partir do Lote ID: " + lotePrincipal.getId())
                .build();
        loteRetalho.getMovimentacoes().add(entradaRetalho);
        loteMateriaPrimaRepository.save(loteRetalho);
    }

    private LoteMateriaPrima findLoteById(Long id) {
        return loteMateriaPrimaRepository.findById(id)
                .orElseThrow(() -> new LoteMateriaPrimaNotFoundException(id));
    }

    private Produto findProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    private BigDecimal getLarguraEmCm(Map<String, Object> atributos) {
        Object larguraMmObj = atributos.get("larguraMm");
        if (!(larguraMmObj instanceof Number)) {
            throw new RegraNegocioException("O lote não possui o atributo 'larguraMm' numérico para realizar o cálculo de corte.");
        }
        return new BigDecimal(((Number) larguraMmObj).intValue()).divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);
    }
}


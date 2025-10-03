package com.dcriar.domain.production.service;

import com.dcriar.api.dto.request.production.MargensRequestDTO;
import com.dcriar.api.dto.response.production.CorteRealizadoDTO;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.production.model.ParametrosCorte;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import com.dcriar.exception.custom.RegraNegocioException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CorteCalculatorService {

    public ParametrosCorte extrairParametrosCorte(
            int quantidade,
            Produto produto,
            LoteMateriaPrima lotePrincipal,
            MargensRequestDTO margensRequest
    ) {
        BigDecimal larguraTotalLoteCm = getLarguraEmCm(lotePrincipal.getAtributos());
        BigDecimal larguraProduto = produto.getDimensoesUnitarias().getLarguraCm();
        BigDecimal comprimentoProduto = produto.getDimensoesUnitarias().getComprimentoCm();
        BigDecimal margemEsquerda = Optional.ofNullable(margensRequest != null ? margensRequest.getEsquerda() : null).orElse(BigDecimal.ZERO);
        BigDecimal margemDireita = Optional.ofNullable(margensRequest != null ? margensRequest.getDireita() : null).orElse(BigDecimal.ZERO);
        BigDecimal larguraUtilCm = calcularLarguraUtilCm(larguraTotalLoteCm, margemEsquerda, margemDireita);

        if (larguraUtilCm.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("A soma das margens laterais não pode exceder a largura do lote.");
        }

        int produtosPorLinha = calcularProdutosPorLinha(larguraUtilCm, larguraProduto);
        if (produtosPorLinha < 1) {
            throw new RegraNegocioException("O produto não cabe na largura útil do lote (considerando as margens).");
        }

        int linhas = calcularLinhas(quantidade, produtosPorLinha);

        return ParametrosCorte.builder()
                .larguraTotalLoteCm(larguraTotalLoteCm)
                .larguraProduto(larguraProduto)
                .comprimentoProduto(comprimentoProduto)
                .quantidade(quantidade)
                .margemEsquerda(margemEsquerda)
                .margemDireita(margemDireita)
                .larguraUtilCm(larguraUtilCm)
                .produtosPorLinha(produtosPorLinha)
                .linhas(linhas)
                .build();
    }

    public BigDecimal calcularComprimentoFinal(BigDecimal comprimentoProduto, int linhas, MargensRequestDTO margens) {
        BigDecimal comprimentoFinal = comprimentoProduto.multiply(new BigDecimal(linhas));
        if (margens != null) {
            comprimentoFinal = comprimentoFinal
                    .add(Optional.ofNullable(margens.getSuperior()).orElse(BigDecimal.ZERO))
                    .add(Optional.ofNullable(margens.getInferior()).orElse(BigDecimal.ZERO));
        }
        return comprimentoFinal;
    }

    public BigDecimal calcularConsumoTotalMetros(List<CorteRealizadoDTO> cortesRealizados) {
        BigDecimal consumoTotalMetros = BigDecimal.ZERO;
        if (cortesRealizados == null) {
            return consumoTotalMetros;
        }
        for (CorteRealizadoDTO corte : cortesRealizados) {
            if ("PRODUTO".equals(corte.getTipo()) || "RETALHO".equals(corte.getTipo())) {
                BigDecimal comprimentoEmMetros = corte.getComprimentoCm().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                consumoTotalMetros = consumoTotalMetros.add(
                    comprimentoEmMetros.multiply(new BigDecimal(corte.getQuantidade()))
                );
            }
        }
        return consumoTotalMetros;
    }

    private BigDecimal getLarguraEmCm(Map<String, Object> atributos) {
        Object larguraMmObj = atributos.get("larguraMm");
        if (!(larguraMmObj instanceof Number)) {
            throw new RegraNegocioException("O atributo 'larguraMm' do lote é inválido ou não existe.");
        }
        return new BigDecimal(((Number) larguraMmObj).intValue()).divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularLarguraUtilCm(BigDecimal larguraTotalLoteCm, BigDecimal margemEsquerda, BigDecimal margemDireita) {
        return larguraTotalLoteCm.subtract(margemEsquerda).subtract(margemDireita);
    }

    private int calcularProdutosPorLinha(BigDecimal larguraUtilCm, BigDecimal larguraProduto) {
        if (larguraProduto == null || larguraProduto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("A largura do produto para cálculo deve ser maior que zero.");
        }
        return larguraUtilCm.divide(larguraProduto, 0, RoundingMode.FLOOR).intValue();
    }

    private int calcularLinhas(int quantidade, int produtosPorLinha) {
        if (produtosPorLinha <= 0) {
            throw new RegraNegocioException("A quantidade de produtos por linha deve ser maior que zero para calcular o número de linhas.");
        }
        return (int) Math.ceil((double) quantidade / produtosPorLinha);
    }
}

package com.dcriar.domain.production.service;

import com.dcriar.api.dto.request.production.MargensRequestDTO;
import com.dcriar.domain.product.entity.Dimensoes;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.production.model.ParametrosCorte;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import com.dcriar.exception.custom.RegraNegocioException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

/**
 * Serviço responsável por realizar os cálculos geométricos para o planeamento de ordens de corte.
 * Esta classe determina a melhor orientação da peça (normal ou rotacionada) para minimizar o consumo
 * de matéria-prima e extrai os parâmetros essenciais para a execução do corte.
 */
@Component
public class CorteCalculatorService {

    /**
     * Extrai os parâmetros de corte otimizados para uma dada produção.
     * <p>
     * O método calcula a melhor forma de arranjar as peças no lote de matéria-prima,
     * testando a orientação normal e a rotacionada (90 graus) do produto. A orientação
     * que resultar no menor consumo de comprimento linear do lote será a escolhida.
     *
     * @param quantidade A quantidade de produtos a serem produzidos.
     * @param produto O produto a ser cortado.
     * @param lotePrincipal O lote de matéria-prima a ser utilizado.
     * @param margensRequest As margens de segurança a serem aplicadas no corte.
     * @return um objeto {@link ParametrosCorte} contendo os dados calculados para o layout de corte mais eficiente.
     * @throws RegraNegocioException se o produto não couber na largura do lote em nenhuma orientação.
     */
    public ParametrosCorte extrairParametrosCorte(
            int quantidade,
            Produto produto,
            LoteMateriaPrima lotePrincipal,
            MargensRequestDTO margensRequest
    ) {
        BigDecimal larguraTotalLoteCm = getLarguraEmCm(lotePrincipal.getAtributos());
        BigDecimal margemEsquerda = Optional.ofNullable(margensRequest != null ? margensRequest.getEsquerda() : null).orElse(BigDecimal.ZERO);
        BigDecimal margemDireita = Optional.ofNullable(margensRequest != null ? margensRequest.getDireita() : null).orElse(BigDecimal.ZERO);
        BigDecimal larguraUtilCm = calcularLarguraUtilCm(larguraTotalLoteCm, margemEsquerda, margemDireita);

        if (larguraUtilCm.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("A soma das margens laterais não pode exceder a largura do lote.");
        }

        Dimensoes dimensoesProduto = produto.getDimensoesUnitarias();
        BigDecimal larguraProduto = dimensoesProduto.getLarguraCm();
        BigDecimal comprimentoProduto = dimensoesProduto.getComprimentoCm();

        // --- Lógica de Otimização por Rotação ---

        // Simulação 1: Orientação Normal
        int produtosPorLinhaNormal = calcularProdutosPorLinha(larguraUtilCm, larguraProduto);
        int linhasNormal = calcularLinhas(quantidade, produtosPorLinhaNormal);
        BigDecimal comprimentoTotalNormal = (produtosPorLinhaNormal > 0)
                ? comprimentoProduto.multiply(new BigDecimal(linhasNormal))
                : BigDecimal.valueOf(Long.MAX_VALUE);

        // Simulação 2: Orientação Rotacionada
        int produtosPorLinhaRotacionado = calcularProdutosPorLinha(larguraUtilCm, comprimentoProduto);
        int linhasRotacionado = calcularLinhas(quantidade, produtosPorLinhaRotacionado);
        BigDecimal comprimentoTotalRotacionado = (produtosPorLinhaRotacionado > 0)
                ? larguraProduto.multiply(new BigDecimal(linhasRotacionado))
                : BigDecimal.valueOf(Long.MAX_VALUE);

        // Decisão: Escolhe a orientação que resulta no menor consumo
        boolean isRotated;
        if (produtosPorLinhaNormal == 0 && produtosPorLinhaRotacionado == 0) {
            throw new RegraNegocioException("O produto não cabe na largura útil do lote em nenhuma orientação.");
        } else {
            isRotated = comprimentoTotalRotacionado.compareTo(comprimentoTotalNormal) < 0;
        }

        BigDecimal pLarguraFinal = isRotated ? comprimentoProduto : larguraProduto;
        BigDecimal pComprimentoFinal = isRotated ? larguraProduto : comprimentoProduto;
        int pProdutosPorLinhaFinal = isRotated ? produtosPorLinhaRotacionado : produtosPorLinhaNormal;

        return new ParametrosCorte(
                larguraTotalLoteCm,
                pLarguraFinal,
                pComprimentoFinal,
                quantidade,
                margemEsquerda,
                margemDireita,
                larguraUtilCm,
                pProdutosPorLinhaFinal,
                    isRotated
        );
    }

    private BigDecimal getLarguraEmCm(Map<String, Object> atributos) {
        Object larguraMmObj = atributos.get("larguraMm");
        if (!(larguraMmObj instanceof Number)) {
            throw new RegraNegocioException("O atributo 'larguraMm' do lote é inválido ou não existe.");
        }
        return new BigDecimal(larguraMmObj.toString()).divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularLarguraUtilCm(BigDecimal larguraTotalLoteCm, BigDecimal margemEsquerda, BigDecimal margemDireita) {
        return larguraTotalLoteCm.subtract(margemEsquerda).subtract(margemDireita);
    }

    private int calcularProdutosPorLinha(BigDecimal larguraUtilCm, BigDecimal dimensaoProduto) {
        if (dimensaoProduto == null || dimensaoProduto.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return larguraUtilCm.divide(dimensaoProduto, 0, RoundingMode.FLOOR).intValue();
    }

    private int calcularLinhas(int quantidade, int produtosPorLinha) {
        if (produtosPorLinha <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) quantidade / produtosPorLinha);
    }
}

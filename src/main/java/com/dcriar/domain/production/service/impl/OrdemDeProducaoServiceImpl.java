package com.dcriar.domain.production.service.impl;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.response.production.OrdemDeCorteResponseDTO;
import com.dcriar.api.dto.request.production.MargensRequestDTO;
import com.dcriar.api.dto.response.production.TamanhoFinalResponseDTO;
import com.dcriar.api.dto.response.production.CorteRealizadoDTO;
import com.dcriar.domain.product.entity.Dimensoes;
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.entity.enums.TipoMovimentacaoProduto;
import com.dcriar.domain.production.enums.ModoCalculo;
import com.dcriar.domain.production.service.OrdemDeProducaoService;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.product.service.EstoqueProdutoService;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import com.dcriar.domain.stock.entity.MovimentacaoEstoqueLote;
import com.dcriar.domain.stock.entity.enums.TipoMovimentacao;
import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import com.dcriar.domain.stock.repository.LoteMateriaPrimaRepository;
import com.dcriar.domain.stock.repository.MovimentacaoEstoqueLoteRepository;
import com.dcriar.domain.production.entity.OrdemDeCorte;
import com.dcriar.domain.production.repository.OrdemDeCorteRepository;
import com.dcriar.exception.custom.LoteMateriaPrimaNotFoundException;
import com.dcriar.exception.custom.ProdutoNotFoundException;
import com.dcriar.exception.custom.RegraNegocioException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrdemDeProducaoServiceImpl implements OrdemDeProducaoService {

    private final LoteMateriaPrimaRepository loteMateriaPrimaRepository;
    private final MovimentacaoEstoqueLoteRepository movimentacaoEstoqueLoteRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final EstoqueProdutoService estoqueProdutoService;
    private final OrdemDeCorteRepository ordemDeCorteRepository;

    /**
     * Processa uma ordem de corte, realizando validações, cálculos e registros necessários.
     *
     * @param requestDTO Dados da requisição da ordem de corte
     * @return DTO de resposta com detalhes da ordem processada
     */
    @Override
    @Transactional
    public OrdemDeCorteResponseDTO processarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO) {
        LoteMateriaPrima lotePrincipal = findLoteById(requestDTO.getLotePrincipalId());
        Produto produto = findProdutoById(requestDTO.getProdutoId());

        // 1. Determina as dimensões finais do corte
        Dimensoes dimensoesFinais = calcularDimensoesFinais(requestDTO, produto);

        // 2. Validações de negócio
        validarOrdemDeCorte(lotePrincipal, dimensoesFinais);

        // 3. Cálculo do consumo e cortes realizados
        ParametrosCorte parametros = extrairParametrosCorte(requestDTO, lotePrincipal, produto);
        ArrayList<CorteRealizadoDTO> cortesRealizados = gerarCortesRealizados(
            parametros.getQuantidade(), parametros.getProdutosPorLinha(), parametros.getLinhas(), parametros.getLarguraProduto(), parametros.getComprimentoProduto(), parametros.getLarguraTotalLoteCm(), parametros.getMargemEsquerda(), parametros.getMargemDireita(), lotePrincipal
        );
        BigDecimal consumoTotalMetros = calcularConsumoTotalMetros(cortesRealizados);

        // 4. Cria a Ordem de Corte no banco usando builder
        OrdemDeCorte ordem = OrdemDeCorte.builder()
                .produto(produto)
                .lotePrincipalId(requestDTO.getLotePrincipalId())
                .quantidadeProduzida(requestDTO.getQuantidadeProduzida())
                .modoCalculo(requestDTO.getModoCalculo() != null ? requestDTO.getModoCalculo() : ModoCalculo.AUTOMATICO)
                .larguraFinalCm(dimensoesFinais.getLarguraCm())
                .comprimentoFinalCm(dimensoesFinais.getComprimentoCm())
                .dataCriacao(java.time.OffsetDateTime.now())
                .motivo(requestDTO.getMotivo())
                .build();
        ordemDeCorteRepository.save(ordem);

        // 5. Registra a saída do lote principal (consumo total)
        registrarSaida(lotePrincipal, consumoTotalMetros, requestDTO.getMotivo());

        // 6. Registra entrada do produto acabado
        registrarEntradaProdutoAcabado(requestDTO, produto);

        // 7. Ajusta estoque para o canal
        distribuirEstoqueParaCanal(requestDTO);

        // Monta o DTO de resposta
        MargensRequestDTO margensDTO = requestDTO.getMargens();
        TamanhoFinalResponseDTO tamanhoFinalDTO = TamanhoFinalResponseDTO.builder()
                .larguraCm(dimensoesFinais.getLarguraCm())
                .comprimentoCm(dimensoesFinais.getComprimentoCm())
                .build();

        return OrdemDeCorteResponseDTO.builder()
                .id(ordem.getId())
                .produtoId(produto.getId())
                .lotePrincipalId(lotePrincipal.getId())
                .canalVendaDestinoId(requestDTO.getCanalVendaDestinoId())
                .quantidadeProduzida(requestDTO.getQuantidadeProduzida())
                .motivo(requestDTO.getMotivo())
                .modoCalculo(ordem.getModoCalculo().name())
                .margens(margensDTO)
                .tamanhoFinal(tamanhoFinalDTO)
                .cortesRealizados(cortesRealizados)
                .build();
    }

    /**
     * Extrai e valida os parâmetros de corte a partir do request, lote e produto.
     * Centraliza a lógica para evitar duplicidade de código.
     *
     * @param requestDTO Dados da requisição da ordem de corte
     * @param lotePrincipal Lote de matéria-prima principal
     * @param produto Produto a ser produzido
     * @return Parâmetros de corte extraídos e validados
     */
    private ParametrosCorte extrairParametrosCorte(OrdemDeCorteRequestDTO requestDTO, LoteMateriaPrima lotePrincipal, Produto produto) {
        BigDecimal larguraTotalLoteCm = getLarguraEmCm(lotePrincipal.getAtributos());
        BigDecimal larguraProduto = produto.getDimensoesUnitarias().getLarguraCm();
        BigDecimal comprimentoProduto = produto.getDimensoesUnitarias().getComprimentoCm();
        int quantidade = requestDTO.getQuantidadeProduzida();
        BigDecimal margemEsquerda = Optional.ofNullable(requestDTO.getMargens() != null ? requestDTO.getMargens().getEsquerda() : null).orElse(BigDecimal.ZERO);
        BigDecimal margemDireita = Optional.ofNullable(requestDTO.getMargens() != null ? requestDTO.getMargens().getDireita() : null).orElse(BigDecimal.ZERO);
        BigDecimal larguraUtilCm = calcularLarguraUtilCm(larguraTotalLoteCm, margemEsquerda, margemDireita);
        if (larguraUtilCm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Margens excedem a largura do lote.");
        }
        int produtosPorLinha = calcularProdutosPorLinha(larguraUtilCm, larguraProduto);
        if (produtosPorLinha < 1) {
            throw new RegraNegocioException("Produto não cabe na largura útil do lote considerando as margens.");
        }
        int linhas = calcularLinhas(quantidade, produtosPorLinha);
        return new ParametrosCorte(larguraTotalLoteCm, larguraProduto, comprimentoProduto, quantidade, margemEsquerda, margemDireita, larguraUtilCm, produtosPorLinha, linhas);
    }

    /**
     * Calcula a quantidade de produtos por linha com base na largura útil e largura do produto.
     *
     * @param larguraUtilCm Largura útil do lote em centímetros
     * @param larguraProduto Largura do produto em centímetros
     * @return Quantidade de produtos por linha
     */
    private int calcularProdutosPorLinha(BigDecimal larguraUtilCm, BigDecimal larguraProduto) {
        return larguraUtilCm.divide(larguraProduto, 0, RoundingMode.FLOOR).intValue();
    }

    // Método extraído para calcular o número de linhas
    private int calcularLinhas(int quantidade, int produtosPorLinha) {
        return (int) Math.ceil((double) quantidade / produtosPorLinha);
    }

    // Classe auxiliar para acumulação de retalho
    private static class RetalhoAcumulado {
        BigDecimal largura;
        int comprimento;
        boolean toAdd;
        RetalhoAcumulado(BigDecimal largura, int comprimento, boolean toAdd) {
            this.largura = largura;
            this.comprimento = comprimento;
            this.toAdd = toAdd;
        }
    }

    // Método extraído para lógica de acumulação de retalho
    private RetalhoAcumulado acumularRetalho(BigDecimal retalhoLarguraAcumulado, int retalhoComprimentoAcumulado, BigDecimal larguraRetalhoLinha, BigDecimal comprimentoProduto) {
        if (retalhoLarguraAcumulado != null && larguraRetalhoLinha.compareTo(retalhoLarguraAcumulado) == 0) {
            return new RetalhoAcumulado(retalhoLarguraAcumulado, retalhoComprimentoAcumulado + comprimentoProduto.intValue(), false);
        } else {
            boolean toAdd = retalhoLarguraAcumulado != null && retalhoComprimentoAcumulado > 0;
            return new RetalhoAcumulado(larguraRetalhoLinha, comprimentoProduto.intValue(), toAdd);
        }
    }

    // Método extraído para calcular larguraUtilCm
    private BigDecimal calcularLarguraUtilCm(BigDecimal larguraTotalLoteCm, BigDecimal margemEsquerda, BigDecimal margemDireita) {
        return larguraTotalLoteCm.subtract(margemEsquerda).subtract(margemDireita);
    }

    // Método extraído para calcular o comprimento final
    private BigDecimal calcularComprimentoFinal(BigDecimal comprimentoProduto, int linhas, MargensRequestDTO margens) {
        BigDecimal comprimentoFinal = comprimentoProduto.multiply(new BigDecimal(linhas));
        if (margens != null) {
            comprimentoFinal = comprimentoFinal
                    .add(Optional.ofNullable(margens.getSuperior()).orElse(BigDecimal.ZERO))
                    .add(Optional.ofNullable(margens.getInferior()).orElse(BigDecimal.ZERO));
        }
        return comprimentoFinal;
    }

    // Método extraído para criar CorteRealizadoDTO para produto
    private CorteRealizadoDTO criarCorteProduto(BigDecimal larguraProduto, BigDecimal comprimentoProduto, int quantidade) {
        return CorteRealizadoDTO.builder()
                .larguraCm(larguraProduto)
                .comprimentoCm(comprimentoProduto)
                .quantidade(quantidade)
                .tipo("PRODUTO")
                .build();
    }

    // Método extraído para criar CorteRealizadoDTO para retalho
    private CorteRealizadoDTO criarCorteRetalho(BigDecimal largura, int comprimento) {
        return CorteRealizadoDTO.builder()
                .larguraCm(largura)
                .comprimentoCm(new BigDecimal(comprimento))
                .quantidade(1)
                .tipo("RETALHO")
                .build();
    }

    private Dimensoes calcularDimensoesFinais(OrdemDeCorteRequestDTO requestDTO, Produto produto) {
        if (requestDTO.getModoCalculo() == ModoCalculo.MANUAL) {
            if (requestDTO.getTamanhoFinal() == null
                    || requestDTO.getTamanhoFinal().getLarguraCm() == null
                    || requestDTO.getTamanhoFinal().getComprimentoCm() == null) {
                throw new RegraNegocioException("Para o modo MANUAL, as dimensões finais são obrigatórias.");
            }
            return new Dimensoes(
                    requestDTO.getTamanhoFinal().getLarguraCm(),
                    requestDTO.getTamanhoFinal().getComprimentoCm()
            );
        }
        if (produto.getDimensoesUnitarias() == null) {
            throw new RegraNegocioException("Produto não possui dimensões unitárias para cálculo automático.");
        }
        LoteMateriaPrima lotePrincipal = findLoteById(requestDTO.getLotePrincipalId());
        ParametrosCorte parametros = extrairParametrosCorte(requestDTO, lotePrincipal, produto);
        BigDecimal comprimentoFinal = calcularComprimentoFinal(parametros.getComprimentoProduto(), parametros.getLinhas(), requestDTO.getMargens());
        return new Dimensoes(parametros.getLarguraTotalLoteCm(), comprimentoFinal);
    }

    private void validarOrdemDeCorte(LoteMateriaPrima lote, Dimensoes dimensoesFinais) {
        if (lote.getUnidadeDeEstoque() != UnidadeDeMedida.METRO_LINEAR) {
            throw new RegraNegocioException("Ordens de corte só podem ser processadas em lotes METRO_LINEAR.");
        }
        BigDecimal saldoAtualMetros = movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
        BigDecimal comprimentoDeCorteMetros = dimensoesFinais.getComprimentoCm()
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        if (comprimentoDeCorteMetros.compareTo(saldoAtualMetros) > 0) {
            throw new RegraNegocioException(
                    "Saldo insuficiente. Necessário: " + comprimentoDeCorteMetros + "m, Disponível: " + saldoAtualMetros + "m."
            );
        }
    }

    private void registrarEntradaProdutoAcabado(OrdemDeCorteRequestDTO requestDTO, Produto produto) {
        MovimentacaoEstoqueProduto entradaProducao = MovimentacaoEstoqueProduto.builder()
                .produto(produto)
                .tipo(TipoMovimentacaoProduto.ENTRADA_PRODUCAO)
                .quantidade(requestDTO.getQuantidadeProduzida())
                .motivo("Produzido via Ordem de Corte. Consumiu Lote ID: " + requestDTO.getLotePrincipalId())
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
        if (larguraSobraCm.compareTo(BigDecimal.ZERO) <= 0) {
            return; // Não cria lote de retalho se a sobra for zero ou negativa
        }

        // Atributos do novo lote de retalho
        Map<String, Object> novosAtributos = Map.of(
                "larguraMm", larguraSobraCm.multiply(new BigDecimal("10")).intValue(),
                "comprimentoMm", comprimentoMetros.multiply(new BigDecimal("100")).intValue() // Comprimento em milímetros
        );

        LoteMateriaPrima loteRetalho = LoteMateriaPrima.builder()
                .tipoMateriaPrima(lotePrincipal.getTipoMateriaPrima())
                .unidadeDeEstoque(UnidadeDeMedida.METRO_LINEAR)
                .atributos(novosAtributos)
                .loteDeOrigem(lotePrincipal)
                .build();

        // Movimentação de entrada do retalho
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
            throw new RegraNegocioException("O lote não possui 'larguraMm' numérico.");
        }
        return new BigDecimal(((Number) larguraMmObj).intValue()).divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void excluirOrdemDeCorte(Long id) {
        OrdemDeCorte ordem = ordemDeCorteRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Ordem de corte não encontrada para exclusão: id=" + id));
        ordemDeCorteRepository.delete(ordem);
    }

    /**
     * Calcula o consumo total em metros a partir da lista de cortes realizados.
     *
     * @param cortesRealizados Lista de cortes realizados (produtos e retalhos)
     * @return Consumo total em metros
     */
    private BigDecimal calcularConsumoTotalMetros(ArrayList<CorteRealizadoDTO> cortesRealizados) {
        BigDecimal consumoTotalMetros = BigDecimal.ZERO;
        for (CorteRealizadoDTO corte : cortesRealizados) {
            if (corte.getTipo().equals("PRODUTO") || corte.getTipo().equals("RETALHO")) {
                consumoTotalMetros = consumoTotalMetros.add(
                    corte.getComprimentoCm().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(corte.getQuantidade()))
                );
            }
        }
        return consumoTotalMetros;
    }

    // Helper to generate cortes realizados and handle retalhos (eliminates duplicated code)
    private ArrayList<CorteRealizadoDTO> gerarCortesRealizados(
            int quantidade, int produtosPorLinha, int linhas,
            BigDecimal larguraProduto, BigDecimal comprimentoProduto,
            BigDecimal larguraTotalLoteCm, BigDecimal margemEsquerda, BigDecimal margemDireita,
            LoteMateriaPrima lotePrincipal
    ) {
        ArrayList<CorteRealizadoDTO> cortesRealizados = new ArrayList<>();
        int produtosRestantes = quantidade;
        BigDecimal retalhoLarguraAcumulado = null;
        int retalhoComprimentoAcumulado = 0;
        for (int linha = 0; linha < linhas; linha++) {
            int produtosNaLinha = Math.min(produtosPorLinha, produtosRestantes);
            if (produtosNaLinha > 0) {
                cortesRealizados.add(criarCorteProduto(larguraProduto, comprimentoProduto, produtosNaLinha));
            }
            BigDecimal larguraProdutosOcupadaLinha = larguraProduto.multiply(new BigDecimal(produtosNaLinha));
            BigDecimal larguraRetalhoLinha = calcularLarguraUtilCm(larguraTotalLoteCm, margemEsquerda, margemDireita).subtract(larguraProdutosOcupadaLinha);
            if (larguraRetalhoLinha.compareTo(BigDecimal.ZERO) > 0 && produtosNaLinha > 0) {
                RetalhoAcumulado retalho = acumularRetalho(retalhoLarguraAcumulado, retalhoComprimentoAcumulado, larguraRetalhoLinha, comprimentoProduto);
                if (retalho.toAdd) {
                    cortesRealizados.add(criarCorteRetalho(retalhoLarguraAcumulado, retalhoComprimentoAcumulado));
                    if (retalhoLarguraAcumulado != null) {
                        criarLoteDeRetalho(lotePrincipal, retalhoLarguraAcumulado, new BigDecimal(retalhoComprimentoAcumulado).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    }
                }
                retalhoLarguraAcumulado = retalho.largura;
                retalhoComprimentoAcumulado = retalho.comprimento;
            }
            produtosRestantes -= produtosNaLinha;
        }
        if (retalhoLarguraAcumulado != null && retalhoComprimentoAcumulado > 0) {
            cortesRealizados.add(criarCorteRetalho(retalhoLarguraAcumulado, retalhoComprimentoAcumulado));
            criarLoteDeRetalho(lotePrincipal, retalhoLarguraAcumulado, new BigDecimal(retalhoComprimentoAcumulado).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        }
        return cortesRealizados;
    }
}

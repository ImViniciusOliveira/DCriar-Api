package com.dcriar.domain.production.service.impl;

import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.request.production.OrdemDeConsumoDiretoRequestDTO;
import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.request.production.SimulacaoConsumoDiretoRequestDTO;
import com.dcriar.api.dto.request.production.SimulacaoCorteRequestDTO;
import com.dcriar.api.dto.response.production.CorteRealizadoDTO;
import com.dcriar.api.dto.response.production.OrdemDeProducaoResponseDTO;
import com.dcriar.api.dto.response.production.SimulacaoConsumoDiretoResponseDTO;
import com.dcriar.api.dto.response.production.SimulacaoCorteResponseDTO;
import com.dcriar.api.mapper.production.OrdemDeProducaoMapper;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.entity.enums.TipoMovimentacaoProduto;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.product.service.EstoqueProdutoService;
import com.dcriar.domain.production.entity.CorteRealizado;
import com.dcriar.domain.production.entity.OrdemDeProducao;
import com.dcriar.domain.production.enums.ModoCalculo;
import com.dcriar.domain.production.model.ParametrosCorte;
import com.dcriar.domain.production.repository.OrdemDeProducaoRepository;
import com.dcriar.domain.production.service.CorteCalculatorService;
import com.dcriar.domain.production.service.OrdemDeProducaoService;
import com.dcriar.domain.stock.entity.LoteMateriaPrima;
import com.dcriar.domain.stock.entity.MovimentacaoEstoqueLote;
import com.dcriar.domain.stock.entity.enums.TipoMovimentacao;
import com.dcriar.domain.stock.entity.enums.UnidadeDeMedida;
import com.dcriar.domain.stock.repository.LoteMateriaPrimaRepository;
import com.dcriar.domain.stock.repository.MovimentacaoEstoqueLoteRepository;
import com.dcriar.exception.custom.LoteMateriaPrimaNotFoundException;
import com.dcriar.exception.custom.ProdutoNotFoundException;
import com.dcriar.exception.custom.RegraNegocioException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Implementação do serviço para gerir Ordens de Produção.
 * Esta classe orquestra a criação de ordens por corte (com otimização de layout)
 * e por consumo direto, gerindo a movimentação de stock de matéria-prima e produtos acabados.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrdemDeProducaoServiceImpl implements OrdemDeProducaoService {

    private final OrdemDeProducaoRepository ordemDeProducaoRepository;
    private final ProdutoRepository produtoRepository;
    private final LoteMateriaPrimaRepository loteMateriaPrimaRepository;
    private final MovimentacaoEstoqueLoteRepository movimentacaoEstoqueLoteRepository;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final OrdemDeProducaoMapper ordemDeProducaoMapper;
    private final CorteCalculatorService corteCalculatorService;
    private final EstoqueProdutoService estoqueProdutoService;

    /**
     * Cria uma nova Ordem de Produção baseada num processo de corte geométrico.
     * A lógica otimiza o uso da matéria-prima, calcula o layout, gera os cortes de produtos
     * e transforma as sobras (retalhos) em novos lotes de stock utilizáveis.
     *
     * @param requestDTO Dados da ordem de corte a ser criada.
     * @return DTO da ordem de produção criada e guardada.
     */
    @Override
    @Transactional
    public OrdemDeProducaoResponseDTO criarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (!isGeometricUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new RegraNegocioException("Este produto não pode ser produzido por corte. Utilize o endpoint de consumo direto.");
        }

        if (requestDTO.getLotePrincipalId() == null) {
            throw new RegraNegocioException("A produção por corte exige a especificação de um 'lotePrincipalId'.");
        }
        LoteMateriaPrima lotePrincipal = findLoteById(requestDTO.getLotePrincipalId());

        BigDecimal consumoTotalMetros;
        BigDecimal comprimentoFinalCm;
        List<CorteRealizadoDTO> cortesRealizadosDTOs;
        BigDecimal larguraFinalCm;
        ParametrosCorte parametros = null;

        if (requestDTO.getModoCalculo() == ModoCalculo.MANUAL) {
            if (requestDTO.getLarguraFinalCm() == null || requestDTO.getComprimentoFinalCm() == null) {
                throw new RegraNegocioException("Para o modo MANUAL, as dimensões finais são obrigatórias.");
            }
            larguraFinalCm = requestDTO.getLarguraFinalCm();
            comprimentoFinalCm = requestDTO.getComprimentoFinalCm();
            consumoTotalMetros = comprimentoFinalCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            cortesRealizadosDTOs = List.of(criarCorteProduto(produto.getDimensoesUnitarias().getLarguraCm(), produto.getDimensoesUnitarias().getComprimentoCm(), requestDTO.getQuantidadeProduzida()));
        } else {
            parametros = corteCalculatorService.extrairParametrosCorte(requestDTO.getQuantidadeProduzida(), produto, lotePrincipal, requestDTO.getMargens());
            cortesRealizadosDTOs = gerarCortesRealizadosDinamico(parametros, lotePrincipal);

            long numeroDeLinhas = cortesRealizadosDTOs.stream().filter(c -> "PRODUTO".equals(c.getTipo())).count();
            comprimentoFinalCm = parametros.comprimentoProduto().multiply(new BigDecimal(numeroDeLinhas));

            if (requestDTO.getMargens() != null) {
                comprimentoFinalCm = comprimentoFinalCm
                        .add(Optional.ofNullable(requestDTO.getMargens().getSuperior()).orElse(BigDecimal.ZERO))
                        .add(Optional.ofNullable(requestDTO.getMargens().getInferior()).orElse(BigDecimal.ZERO));
            }
            consumoTotalMetros = comprimentoFinalCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

            larguraFinalCm = produto.getDimensoesUnitarias().getLarguraCm();
        }

        validarSaldoLoteCorte(lotePrincipal, consumoTotalMetros);

        OrdemDeProducao ordem = OrdemDeProducao.builder()
                .produto(produto)
                .lotesConsumidos(Set.of(lotePrincipal))
                .quantidadeProduzida(requestDTO.getQuantidadeProduzida())
                .modoCalculo(requestDTO.getModoCalculo())
                .margens(ordemDeProducaoMapper.toMargensEntity(requestDTO.getMargens()))
                .larguraFinalCm(larguraFinalCm)
                .comprimentoFinalCm(comprimentoFinalCm)
                .motivo(requestDTO.getMotivo())
                .rotacionado(parametros != null && parametros.rotacionado())
                .build();

        for (CorteRealizadoDTO dto : cortesRealizadosDTOs) {
            ordem.addCorteRealizado(CorteRealizado.builder()
                    .larguraCm(dto.getLarguraCm()).comprimentoCm(dto.getComprimentoCm())
                    .quantidade(dto.getQuantidade()).tipo(dto.getTipo()).build());
        }

        OrdemDeProducao savedOrdem = ordemDeProducaoRepository.save(ordem);

        registrarSaidaLote(lotePrincipal, consumoTotalMetros, "Consumido pela Ordem de Produção #" + savedOrdem.getId());
        registrarEntradaProduto(produto, requestDTO.getQuantidadeProduzida(), "Produzido via Ordem de Produção #" + savedOrdem.getId());
        distribuirEstoqueParaCanal(savedOrdem.getProduto().getId(), requestDTO.getCanalVendaDestinoId(), requestDTO.getQuantidadeProduzida());

        return ordemDeProducaoMapper.toDto(savedOrdem);
    }

    @Override
    @Transactional
    public OrdemDeProducaoResponseDTO criarOrdemDeConsumoDireto(OrdemDeConsumoDiretoRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (!isDirectConsumptionUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new RegraNegocioException("Este produto não pode ser produzido por consumo direto. Utilize o endpoint de corte.");
        }

        Set<LoteMateriaPrima> lotesConsumidos = new HashSet<>(loteMateriaPrimaRepository.findAllById(requestDTO.getLotesConsumidosIds()));
        if (lotesConsumidos.size() != requestDTO.getLotesConsumidosIds().size()) {
            throw new RegraNegocioException("Um ou mais IDs de lote fornecidos são inválidos.");
        }

        BigDecimal consumoTotalNecessario = new BigDecimal(produto.getUnidadesPorProduto() * requestDTO.getQuantidadeProduzida());
        BigDecimal saldoTotalDisponivel = lotesConsumidos.stream()
                .map(movimentacaoEstoqueLoteRepository::findSaldoByLote)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (saldoTotalDisponivel.compareTo(consumoTotalNecessario) < 0) {
            throw new RegraNegocioException("Saldo de matéria-prima insuficiente. Necessário: " + consumoTotalNecessario + ", Disponível: " + saldoTotalDisponivel);
        }

        OrdemDeProducao ordem = OrdemDeProducao.builder()
                .produto(produto)
                .lotesConsumidos(lotesConsumidos)
                .quantidadeProduzida(requestDTO.getQuantidadeProduzida())
                .motivo(requestDTO.getMotivo())
                .build();
        OrdemDeProducao savedOrdem = ordemDeProducaoRepository.save(ordem);

        BigDecimal consumoRestante = consumoTotalNecessario;
        for (LoteMateriaPrima lote : lotesConsumidos) {
            if (consumoRestante.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal saldoDoLote = movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
            BigDecimal consumoNesteLote = saldoDoLote.min(consumoRestante);
            if (consumoNesteLote.compareTo(BigDecimal.ZERO) > 0) {
                registrarSaidaLote(lote, consumoNesteLote, "Consumido pela Ordem de Produção #" + savedOrdem.getId());
                consumoRestante = consumoRestante.subtract(consumoNesteLote);
            }
        }

        registrarEntradaProduto(produto, requestDTO.getQuantidadeProduzida(), "Produzido via Ordem de Produção #" + savedOrdem.getId());
        distribuirEstoqueParaCanal(savedOrdem.getProduto().getId(), requestDTO.getCanalVendaDestinoId(), requestDTO.getQuantidadeProduzida());

        return ordemDeProducaoMapper.toDto(savedOrdem);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        if (!ordemDeProducaoRepository.existsById(id)) {
            throw new RegraNegocioException("Ordem de Produção não encontrada com ID: " + id);
        }
        ordemDeProducaoRepository.deleteById(id);
    }

    @Override
    public OrdemDeProducaoResponseDTO buscarPorId(Long id) {
        return ordemDeProducaoRepository.findById(id)
                .map(ordemDeProducaoMapper::toDto)
                .orElseThrow(() -> new RegraNegocioException("Ordem de Produção não encontrada com ID: " + id));
    }

    @Override
    public List<OrdemDeProducaoResponseDTO> listarTodas() {
        return ordemDeProducaoRepository.findAll().stream()
                .map(ordemDeProducaoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SimulacaoCorteResponseDTO simularCorte(SimulacaoCorteRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (!isGeometricUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new RegraNegocioException("Este produto não utiliza uma matéria-prima geométrica para simulação de corte.");
        }

        LoteMateriaPrima loteParaSimulacao = loteMateriaPrimaRepository.findAllByTipoMateriaPrima(produto.getTipoMateriaPrima()).stream()
                .filter(lote -> movimentacaoEstoqueLoteRepository.findSaldoByLote(lote).compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("Não há lotes de matéria-prima com stock disponível para este produto."));


        ParametrosCorte parametros = corteCalculatorService.extrairParametrosCorte(requestDTO.getQuantidade(), produto, loteParaSimulacao, null);
        long numeroDeLinhas = (long) Math.ceil((double) requestDTO.getQuantidade() / parametros.produtosPorLinha());
        BigDecimal comprimentoFinalCm = parametros.comprimentoProduto().multiply(new BigDecimal(numeroDeLinhas));
        BigDecimal consumoEstimado = comprimentoFinalCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        return SimulacaoCorteResponseDTO.builder()
                .modoCalculo(ModoCalculo.AUTOMATICO)
                .larguraFinalCm(parametros.larguraTotalLoteCm())
                .comprimentoFinalCm(comprimentoFinalCm)
                .consumoEstimado(consumoEstimado)
                .build();
    }

    @Override
    public SimulacaoConsumoDiretoResponseDTO simularConsumoDireto(SimulacaoConsumoDiretoRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (!isDirectConsumptionUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new RegraNegocioException("Este produto utiliza uma matéria-prima geométrica. Utilize o simulador de corte.");
        }

        BigDecimal consumoTotalEstimado = new BigDecimal(produto.getUnidadesPorProduto() * requestDTO.getQuantidade());
        return SimulacaoConsumoDiretoResponseDTO.builder()
                .consumoTotalEstimado(consumoTotalEstimado)
                .unidadeDeConsumo(produto.getTipoMateriaPrima().getUnidadeDeConsumo())
                .build();
    }

    private List<CorteRealizadoDTO> gerarCortesRealizadosDinamico(ParametrosCorte parametros, LoteMateriaPrima lotePrincipal) {
        List<CorteRealizadoDTO> cortesRealizados = new ArrayList<>();
        int produtosRestantes = parametros.quantidade();

        while (produtosRestantes > 0) {
            int produtosNestaLinha = Math.min(parametros.produtosPorLinha(), produtosRestantes);
            if (produtosNestaLinha <= 0) break;

            cortesRealizados.add(criarCorteProduto(parametros.larguraProduto(), parametros.comprimentoProduto(), produtosNestaLinha));
            BigDecimal larguraProdutosOcupada = parametros.larguraProduto().multiply(new BigDecimal(produtosNestaLinha));
            BigDecimal larguraRetalhoLinha = parametros.larguraUtilCm().subtract(larguraProdutosOcupada);

            if (larguraRetalhoLinha.compareTo(BigDecimal.ZERO) > 0) {
                cortesRealizados.add(criarCorteRetalho(larguraRetalhoLinha, parametros.comprimentoProduto()));
                BigDecimal comprimentoRetalhoMetros = parametros.comprimentoProduto().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                criarLoteDeRetalho(lotePrincipal, larguraRetalhoLinha, comprimentoRetalhoMetros);
            }
            produtosRestantes -= produtosNestaLinha;
        }
        return cortesRealizados;
    }

    private CorteRealizadoDTO criarCorteProduto(BigDecimal larguraProduto, BigDecimal comprimentoProduto, int quantidade) {
        return CorteRealizadoDTO.builder()
                .larguraCm(larguraProduto)
                .comprimentoCm(comprimentoProduto)
                .quantidade(quantidade)
                .tipo("PRODUTO")
                .build();
    }

    private CorteRealizadoDTO criarCorteRetalho(BigDecimal largura, BigDecimal comprimento) {
        return CorteRealizadoDTO.builder()
                .larguraCm(largura)
                .comprimentoCm(comprimento)
                .quantidade(1)
                .tipo("RETALHO")
                .build();
    }

    private void criarLoteDeRetalho(LoteMateriaPrima lotePrincipal, BigDecimal larguraSobraCm, BigDecimal comprimentoMetros) {
        if (larguraSobraCm.compareTo(BigDecimal.ZERO) <= 0) return;
        Map<String, Object> novosAtributos = Map.of("larguraMm", larguraSobraCm.multiply(new BigDecimal("10")).intValue());
        LoteMateriaPrima loteRetalho = LoteMateriaPrima.builder()
                .tipoMateriaPrima(lotePrincipal.getTipoMateriaPrima())
                .unidadeDeEstoque(lotePrincipal.getUnidadeDeEstoque())
                .atributos(novosAtributos)
                .loteDeOrigem(lotePrincipal)
                .build();
        MovimentacaoEstoqueLote entradaRetalho = MovimentacaoEstoqueLote.builder()
                .lote(loteRetalho)
                .tipo(TipoMovimentacao.ENTRADA_SOBRA)
                .quantidade(comprimentoMetros)
                .motivo("Retalho gerado pela Ordem de Produção a partir do Lote ID: " + lotePrincipal.getId())
                .build();
        loteRetalho.getMovimentacoes().add(entradaRetalho);
        loteMateriaPrimaRepository.save(loteRetalho);
    }

    private void distribuirEstoqueParaCanal(Long produtoId, Long canalVendaId, Integer quantidade) {
        if (canalVendaId != null && quantidade != null && quantidade > 0) {
            AjusteEstoqueRequestDTO ajusteDTO = AjusteEstoqueRequestDTO.builder()
                    .produtoId(produtoId)
                    .canalVendaId(canalVendaId)
                    .quantidade(quantidade)
                    .build();
            estoqueProdutoService.ajustarEstoque(ajusteDTO);
        }
    }

    private void registrarEntradaProduto(Produto produto, int quantidade, String motivo) {
        MovimentacaoEstoqueProduto entrada = MovimentacaoEstoqueProduto.builder()
                .produto(produto)
                .tipo(TipoMovimentacaoProduto.ENTRADA_PRODUCAO)
                .quantidade(quantidade)
                .motivo(motivo)
                .build();
        movimentacaoEstoqueProdutoRepository.save(entrada);
    }

    private void registrarSaidaLote(LoteMateriaPrima lote, BigDecimal quantidade, String motivo) {
        MovimentacaoEstoqueLote saida = MovimentacaoEstoqueLote.builder()
                .lote(lote)
                .tipo(TipoMovimentacao.SAIDA_PRODUCAO)
                .quantidade(quantidade.negate())
                .motivo(motivo)
                .build();
        movimentacaoEstoqueLoteRepository.save(saida);
    }

    private void validarSaldoLoteCorte(LoteMateriaPrima lote, BigDecimal consumoEmMetros) {
        BigDecimal saldoAtual = movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
        if (consumoEmMetros.compareTo(saldoAtual) > 0) {
            throw new RegraNegocioException(
                    String.format("Saldo insuficiente para o corte. Necessário: %.2f m, Disponível: %.2f m", consumoEmMetros, saldoAtual)
            );
        }
    }

    private Produto findProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    private LoteMateriaPrima findLoteById(Long id) {
        return loteMateriaPrimaRepository.findById(id)
                .orElseThrow(() -> new LoteMateriaPrimaNotFoundException(id));
    }

    private boolean isGeometricUnit(UnidadeDeMedida unidade) {
        return unidade == UnidadeDeMedida.METRO_LINEAR ||
                unidade == UnidadeDeMedida.CENTIMETRO_LINEAR ||
                unidade == UnidadeDeMedida.METRO_QUADRADO ||
                unidade == UnidadeDeMedida.CENTIMETRO_QUADRADO;
    }

    private boolean isDirectConsumptionUnit(UnidadeDeMedida unidade) {
        return unidade == UnidadeDeMedida.LITRO ||
                unidade == UnidadeDeMedida.MILILITRO ||
                unidade == UnidadeDeMedida.QUILOGRAMA ||
                unidade == UnidadeDeMedida.GRAMA ||
                unidade == UnidadeDeMedida.UNIDADE;
    }
}

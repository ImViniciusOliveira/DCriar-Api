package com.dcriar.domain.production.service.impl;

import com.dcriar.api.dto.request.production.OrdemDeConsumoDiretoRequestDTO;
import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.request.production.SimulacaoConsumoDiretoRequestDTO;
import com.dcriar.api.dto.request.production.SimulacaoCorteRequestDTO;
import com.dcriar.api.dto.response.production.OrdemDeProducaoResponseDTO;
import com.dcriar.api.dto.response.production.SimulacaoConsumoDiretoResponseDTO;
import com.dcriar.api.dto.response.production.SimulacaoCorteResponseDTO;
import com.dcriar.api.mapper.production.OrdemDeProducaoMapper;
import com.dcriar.domain.product.entity.Dimensoes;
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.entity.enums.TipoMovimentacaoProduto;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdemDeProducaoServiceImpl implements OrdemDeProducaoService {

    private final OrdemDeProducaoRepository ordemDeProducaoRepository;
    private final ProdutoRepository produtoRepository;
    private final LoteMateriaPrimaRepository loteMateriaPrimaRepository;
    private final MovimentacaoEstoqueLoteRepository movimentacaoEstoqueLoteRepository;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final OrdemDeProducaoMapper ordemDeProducaoMapper;
    private final CorteCalculatorService corteCalculatorService;

    @Override
    @Transactional
    public OrdemDeProducaoResponseDTO criarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (!isGeometricUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new RegraNegocioException("Este produto não pode ser produzido por corte. Use o endpoint de consumo direto.");
        }

        LoteMateriaPrima lotePrincipal = findLoteById(requestDTO.getLotePrincipalId());

        ParametrosCorte parametros = corteCalculatorService.extrairParametrosCorte(requestDTO.getQuantidadeProduzida(), produto, lotePrincipal, requestDTO.getMargens());
        BigDecimal comprimentoFinal = corteCalculatorService.calcularComprimentoFinal(parametros.comprimentoProduto(), parametros.linhas(), requestDTO.getMargens());
        Dimensoes dimensoesFinais = new Dimensoes(parametros.larguraTotalLoteCm(), comprimentoFinal);
        validarSaldoLoteCorte(lotePrincipal, dimensoesFinais);

        OrdemDeProducao ordem = OrdemDeProducao.builder()
                .produto(produto)
                .lotesConsumidos(Set.of(lotePrincipal))
                .quantidadeProduzida(requestDTO.getQuantidadeProduzida())
                .modoCalculo(requestDTO.getModoCalculo())
                .margens(ordemDeProducaoMapper.toMargensEntity(requestDTO.getMargens()))
                .larguraFinalCm(dimensoesFinais.getLarguraCm())
                .comprimentoFinalCm(dimensoesFinais.getComprimentoCm())
                .motivo(requestDTO.getMotivo())
                .build();

        OrdemDeProducao savedOrdem = ordemDeProducaoRepository.save(ordem);

        BigDecimal consumoTotal = corteCalculatorService.calcularConsumoTotal(dimensoesFinais);
        registrarSaidaLote(lotePrincipal, consumoTotal, "Consumido pela Ordem de Produção #" + savedOrdem.getId());
        registrarEntradaProduto(produto, requestDTO.getQuantidadeProduzida(), "Produzido via Ordem de Produção #" + savedOrdem.getId());

        return ordemDeProducaoMapper.toDto(savedOrdem);
    }

    @Override
    @Transactional
    public OrdemDeProducaoResponseDTO criarOrdemDeConsumoDireto(OrdemDeConsumoDiretoRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (!isDirectConsumptionUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new RegraNegocioException("Este produto não pode ser produzido por consumo direto. Use o endpoint de corte.");
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

        return ordemDeProducaoMapper.toDto(savedOrdem);
    }

    @Override
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

        List<LoteMateriaPrima> lotesDisponiveis = loteMateriaPrimaRepository.findAllByTipoMateriaPrima(produto.getTipoMateriaPrima()).stream()
                .filter(lote -> movimentacaoEstoqueLoteRepository.findSaldoByLote(lote).compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (lotesDisponiveis.isEmpty()) {
            throw new RegraNegocioException("Não há lotes de matéria-prima com estoque disponível para este produto.");
        }

        LoteMateriaPrima loteParaSimulacao = lotesDisponiveis.getFirst();

        ParametrosCorte parametros = corteCalculatorService.extrairParametrosCorte(requestDTO.getQuantidade(), produto, loteParaSimulacao, null);
        BigDecimal comprimentoFinal = corteCalculatorService.calcularComprimentoFinal(parametros.comprimentoProduto(), parametros.linhas(), null);
        Dimensoes dimensoesFinais = new Dimensoes(parametros.larguraTotalLoteCm(), comprimentoFinal);

        return SimulacaoCorteResponseDTO.builder()
                .modoCalculo(ModoCalculo.AUTOMATICO)
                .larguraFinalCm(dimensoesFinais.getLarguraCm())
                .comprimentoFinalCm(dimensoesFinais.getComprimentoCm())
                .consumoEstimado(corteCalculatorService.calcularConsumoTotal(dimensoesFinais))
                .build();
    }

    @Override
    public SimulacaoConsumoDiretoResponseDTO simularConsumoDireto(SimulacaoConsumoDiretoRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        UnidadeDeMedida unidadeDeConsumo = produto.getTipoMateriaPrima().getUnidadeDeConsumo();

        if (!isDirectConsumptionUnit(unidadeDeConsumo)) {
            throw new RegraNegocioException("Este produto utiliza uma matéria-prima geométrica. Use o simulador de corte.");
        }

        BigDecimal consumoTotalEstimado = new BigDecimal(produto.getUnidadesPorProduto() * requestDTO.getQuantidade());

        return SimulacaoConsumoDiretoResponseDTO.builder()
                .consumoTotalEstimado(consumoTotalEstimado)
                .unidadeDeConsumo(unidadeDeConsumo)
                .build();
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

    private void validarSaldoLoteCorte(LoteMateriaPrima lote, Dimensoes dimensoesFinais) {
        BigDecimal saldoAtual = movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
        BigDecimal consumoEstimado = corteCalculatorService.calcularConsumoTotal(dimensoesFinais);

        if (consumoEstimado.compareTo(saldoAtual) > 0) {
            throw new RegraNegocioException("Saldo insuficiente para o corte. Necessário: " + consumoEstimado + ", Disponível: " + saldoAtual);
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

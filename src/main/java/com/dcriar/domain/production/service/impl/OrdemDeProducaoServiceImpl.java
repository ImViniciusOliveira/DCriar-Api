package com.dcriar.domain.production.service.impl;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.response.production.CorteRealizadoDTO;
import com.dcriar.api.dto.response.production.OrdemDeCorteResponseDTO;
import com.dcriar.api.mapper.production.OrdemDeCorteMapper;
import com.dcriar.domain.product.entity.Dimensoes;
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
import com.dcriar.domain.product.entity.Produto;
import com.dcriar.domain.product.entity.enums.TipoMovimentacaoProduto;
import com.dcriar.domain.product.repository.MovimentacaoEstoqueProdutoRepository;
import com.dcriar.domain.product.repository.ProdutoRepository;
import com.dcriar.domain.product.service.EstoqueProdutoService;
import com.dcriar.domain.production.entity.CorteRealizado;
import com.dcriar.domain.production.entity.Margens;
import com.dcriar.domain.production.entity.OrdemDeCorte;
import com.dcriar.domain.production.enums.ModoCalculo;
import com.dcriar.domain.production.model.ParametrosCorte;
import com.dcriar.domain.production.repository.OrdemDeCorteRepository;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementação do serviço para gerenciar Ordens de Produção (Corte).
 * <p>
 * Esta classe orquestra a lógica de negócio para criar, buscar, listar e excluir ordens de corte,
 * interagindo com repositórios, serviços de cálculo e outros componentes do domínio.
 */
@Service
@RequiredArgsConstructor
public class OrdemDeProducaoServiceImpl implements OrdemDeProducaoService {

    private final LoteMateriaPrimaRepository loteMateriaPrimaRepository;
    private final MovimentacaoEstoqueLoteRepository movimentacaoEstoqueLoteRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueProdutoRepository movimentacaoEstoqueProdutoRepository;
    private final EstoqueProdutoService estoqueProdutoService;
    private final OrdemDeCorteRepository ordemDeCorteRepository;
    private final OrdemDeCorteMapper ordemDeCorteMapper;
    private final CorteCalculatorService corteCalculatorService;

    /**
     * Processa e cria uma nova Ordem de Corte com base nos dados fornecidos.
     *
     * @param requestDTO O DTO contendo os detalhes da ordem de corte a ser criada.
     * @return Um DTO com os detalhes da ordem de corte que foi criada e salva.
     * @throws RegraNegocioException se alguma validação de negócio falhar (ex: saldo insuficiente).
     */
    @Override
    @Transactional
    public OrdemDeCorteResponseDTO processarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO) {
        LoteMateriaPrima lotePrincipal = findLoteById(requestDTO.getLotePrincipalId());
        Produto produto = findProdutoById(requestDTO.getProdutoId());

        Dimensoes dimensoesFinais = calcularDimensoesFinais(requestDTO, produto);
        validarOrdemDeCorte(lotePrincipal, dimensoesFinais);

        ParametrosCorte parametros = corteCalculatorService.extrairParametrosCorte(
                requestDTO.getQuantidadeProduzida(),
                produto,
                lotePrincipal,
                requestDTO.getMargens()
        );

        List<CorteRealizadoDTO> cortesRealizadosDTOs = gerarCortesRealizados(parametros, lotePrincipal);
        BigDecimal consumoTotalMetros = corteCalculatorService.calcularConsumoTotalMetros(cortesRealizadosDTOs);

        Margens margens = ordemDeCorteMapper.toMargensEntity(requestDTO.getMargens());

        OrdemDeCorte ordem = OrdemDeCorte.builder()
                .produto(produto)
                .lotePrincipalId(requestDTO.getLotePrincipalId())
                .canalVendaDestinoId(requestDTO.getCanalVendaDestinoId())
                .quantidadeProduzida(requestDTO.getQuantidadeProduzida())
                .modoCalculo(requestDTO.getModoCalculo() != null ? requestDTO.getModoCalculo() : ModoCalculo.AUTOMATICO)
                .larguraFinalCm(dimensoesFinais.getLarguraCm())
                .comprimentoFinalCm(dimensoesFinais.getComprimentoCm())
                .motivo(requestDTO.getMotivo())
                .margens(margens)
                .build();

        for (CorteRealizadoDTO dto : cortesRealizadosDTOs) {
            CorteRealizado corte = CorteRealizado.builder()
                    .larguraCm(dto.getLarguraCm())
                    .comprimentoCm(dto.getComprimentoCm())
                    .quantidade(dto.getQuantidade())
                    .tipo(dto.getTipo())
                    .build();
            ordem.addCorteRealizado(corte);
        }

        OrdemDeCorte savedOrdem = ordemDeCorteRepository.save(ordem);

        registrarSaida(lotePrincipal, consumoTotalMetros, requestDTO.getMotivo());
        registrarEntradaProdutoAcabado(requestDTO, produto);
        distribuirEstoqueParaCanal(requestDTO);

        return ordemDeCorteMapper.toDto(savedOrdem);
    }

    /**
     * Busca uma Ordem de Corte pelo seu ID.
     *
     * @param id O ID da ordem de corte a ser buscada.
     * @return Um DTO com os detalhes da ordem de corte encontrada.
     * @throws RegraNegocioException se nenhuma ordem for encontrada com o ID fornecido.
     */
    @Override
    public OrdemDeCorteResponseDTO buscarOrdemDeCortePorId(Long id) {
        OrdemDeCorte ordemDeCorte = ordemDeCorteRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Ordem de Corte não encontrada com ID: " + id));
        return ordemDeCorteMapper.toDto(ordemDeCorte);
    }

    /**
     * Lista todas as Ordens de Corte existentes.
     *
     * @return Uma lista de DTOs, cada um representando uma ordem de corte.
     */
    @Override
    public List<OrdemDeCorteResponseDTO> listarTodasOrdensDeCorte() {
        List<OrdemDeCorte> ordensDeCorte = ordemDeCorteRepository.findAll();
        return ordensDeCorte.stream()
                .map(ordemDeCorteMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Exclui uma Ordem de Corte pelo seu ID.
     *
     * @param id O ID da ordem de corte a ser excluída.
     * @throws RegraNegocioException se nenhuma ordem for encontrada para exclusão.
     */
    @Override
    @Transactional
    public void excluirOrdemDeCorte(Long id) {
        OrdemDeCorte ordem = ordemDeCorteRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Ordem de corte não encontrada para exclusão: id=" + id));
        ordemDeCorteRepository.delete(ordem);
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

    private List<CorteRealizadoDTO> gerarCortesRealizados(
            ParametrosCorte parametros,
            LoteMateriaPrima lotePrincipal
    ) {
        List<CorteRealizadoDTO> cortesRealizados = new ArrayList<>();
        int produtosRestantes = parametros.quantidade();
        BigDecimal retalhoLarguraAcumulado = null;
        BigDecimal retalhoComprimentoAcumulado = BigDecimal.ZERO;

        for (int linha = 0; linha < parametros.linhas(); linha++) {
            int produtosNaLinha = Math.min(parametros.produtosPorLinha(), produtosRestantes);
            if (produtosNaLinha > 0) {
                cortesRealizados.add(criarCorteProduto(parametros.larguraProduto(), parametros.comprimentoProduto(), produtosNaLinha));
            }

            BigDecimal larguraProdutosOcupadaLinha = parametros.larguraProduto().multiply(new BigDecimal(produtosNaLinha));
            BigDecimal larguraRetalhoLinha = parametros.larguraUtilCm().subtract(larguraProdutosOcupadaLinha);

            if (larguraRetalhoLinha.compareTo(BigDecimal.ZERO) > 0 && produtosNaLinha > 0) {
                if (retalhoLarguraAcumulado != null && larguraRetalhoLinha.compareTo(retalhoLarguraAcumulado) == 0) {
                    retalhoComprimentoAcumulado = retalhoComprimentoAcumulado.add(parametros.comprimentoProduto());
                } else {
                    if (retalhoLarguraAcumulado != null && retalhoComprimentoAcumulado.compareTo(BigDecimal.ZERO) > 0) {
                        cortesRealizados.add(criarCorteRetalho(retalhoLarguraAcumulado, retalhoComprimentoAcumulado));
                        criarLoteDeRetalho(lotePrincipal, retalhoLarguraAcumulado, retalhoComprimentoAcumulado.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    }
                    retalhoLarguraAcumulado = larguraRetalhoLinha;
                    retalhoComprimentoAcumulado = parametros.comprimentoProduto();
                }
            }
            produtosRestantes -= produtosNaLinha;
        }

        if (retalhoLarguraAcumulado != null && retalhoComprimentoAcumulado.compareTo(BigDecimal.ZERO) > 0) {
            cortesRealizados.add(criarCorteRetalho(retalhoLarguraAcumulado, retalhoComprimentoAcumulado));
            criarLoteDeRetalho(lotePrincipal, retalhoLarguraAcumulado, retalhoComprimentoAcumulado.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        }

        return cortesRealizados;
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
        ParametrosCorte parametros = corteCalculatorService.extrairParametrosCorte(
                requestDTO.getQuantidadeProduzida(),
                produto,
                lotePrincipal,
                requestDTO.getMargens()
        );

        BigDecimal comprimentoFinal = corteCalculatorService.calcularComprimentoFinal(
                parametros.comprimentoProduto(),
                parametros.linhas(),
                requestDTO.getMargens()
        );
        return new Dimensoes(parametros.larguraTotalLoteCm(), comprimentoFinal);
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
                .quantidade(requestDTO.getQuantidadeProduzida() == null ? 0 : requestDTO.getQuantidadeProduzida())
                .motivo("Produzido via Ordem de Corte. Consumiu Lote ID: " + requestDTO.getLotePrincipalId())
                .build();
        movimentacaoEstoqueProdutoRepository.save(entradaProducao);
    }

    private void distribuirEstoqueParaCanal(OrdemDeCorteRequestDTO requestDTO) {
        AjusteEstoqueRequestDTO ajusteDTO = AjusteEstoqueRequestDTO.builder()
                .produtoId(requestDTO.getProdutoId())
                .canalVendaId(requestDTO.getCanalVendaDestinoId())
                .quantidade(requestDTO.getQuantidadeProduzida() == null ? 0 : requestDTO.getQuantidadeProduzida())
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
            return;
        }

        Map<String, Object> novosAtributos = Map.of(
                "larguraMm", larguraSobraCm.multiply(new BigDecimal("10")).intValue(),
                "comprimentoMm", comprimentoMetros.multiply(new BigDecimal("100")).intValue()
        );

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
}

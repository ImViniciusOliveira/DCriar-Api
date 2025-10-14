package com.dcriar.domain.production.service.impl;

import com.dcriar.api.dto.request.product.AjusteEstoqueRequestDTO;
import com.dcriar.api.dto.request.product.MovimentacaoEstoqueProdutoRequestDTO;
import com.dcriar.api.dto.request.production.*;
import com.dcriar.api.dto.request.stock.MovimentacaoRequestDTO;
import com.dcriar.api.dto.response.production.CorteRealizadoResponseDTO;
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
import com.dcriar.domain.production.entity.Margens;
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
     * {@inheritDoc}
     * <p>
     * A lógica otimiza o uso da matéria-prima, calcula o layout, gera os cortes de produtos
     * e transforma as sobras (retalhos) em novos lotes de stock utilizáveis. Orquestra as seguintes etapas:
     * <ol>
     *     <li>Valida se o produto é compatível com produção por corte.</li>
     *     <li>Calcula os parâmetros de corte (automático) ou usa os dados manuais.</li>
     *     <li>Valida se há saldo suficiente no lote de matéria-prima.</li>
     *     <li>Cria e salva a Ordem de Produção com os detalhes do processo.</li>
     *     <li>Registra a saída no estoque do lote de matéria-prima.</li>
     *     <li>Registra a entrada no estoque do produto acabado.</li>
     *     <li>Distribui o novo estoque de produto para um canal de venda, se especificado.</li>
     * </ol>
     */
    @Override
    @Transactional
    public OrdemDeProducaoResponseDTO criarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (isGeometricUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new RegraNegocioException("Este produto não pode ser produzido por corte. Utilize o endpoint de consumo direto.");
        }

        if (requestDTO.getLotePrincipalId() == null) {
            throw new RegraNegocioException("A produção por corte exige a especificação de um 'lotePrincipalId'.");
        }
        LoteMateriaPrima lotePrincipal = findLoteById(requestDTO.getLotePrincipalId());

        BigDecimal consumoTotalMetros;
        BigDecimal comprimentoFinalCm;
        List<CorteRealizadoResponseDTO> cortesRealizadosDTOs;
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

            long numeroDeLinhas = (long) Math.ceil((double) requestDTO.getQuantidadeProduzida() / parametros.produtosPorLinha());
            comprimentoFinalCm = parametros.comprimentoProduto().multiply(new BigDecimal(numeroDeLinhas));

            if (requestDTO.getMargens() != null) {
                comprimentoFinalCm = comprimentoFinalCm
                        .add(Optional.ofNullable(requestDTO.getMargens().getSuperior()).orElse(BigDecimal.ZERO))
                        .add(Optional.ofNullable(requestDTO.getMargens().getInferior()).orElse(BigDecimal.ZERO));
            }
            consumoTotalMetros = comprimentoFinalCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

            larguraFinalCm = parametros.rotacionado()
                    ? produto.getDimensoesUnitarias().getComprimentoCm()
                    : produto.getDimensoesUnitarias().getLarguraCm();
        }

        validarSaldoLoteCorte(lotePrincipal, consumoTotalMetros);

        // Conversão do DTO específico para OrdemDeProducaoRequestDTO
        OrdemDeProducaoRequestDTO ordemRequestDTO = OrdemDeProducaoRequestDTO.builder()
            .produtoId(produto.getId())
            .lotesConsumidosIds(Set.of(lotePrincipal.getId()))
            .canalVendaDestinoId(requestDTO.getCanalVendaDestinoId())
            .quantidadeProduzida(requestDTO.getQuantidadeProduzida())
            .modoCalculo(requestDTO.getModoCalculo().name())
            .margens(requestDTO.getMargens())
            .larguraFinalCm(larguraFinalCm)
            .comprimentoFinalCm(comprimentoFinalCm)
            .motivo(requestDTO.getMotivo())
            .rotacionado(parametros != null && parametros.rotacionado())
            .build();

        Margens margens = ordemDeProducaoMapper.toMargensEntity(requestDTO.getMargens());
        OrdemDeProducao ordem = OrdemDeProducao.from(ordemRequestDTO, produto, Set.of(lotePrincipal), margens);

        for (CorteRealizadoResponseDTO dto : cortesRealizadosDTOs) {
            ordem.addCorteRealizado(CorteRealizado.from(
                CorteRealizadoRequestDTO.builder()
                    .larguraCm(dto.getLarguraCm())
                    .comprimentoCm(dto.getComprimentoCm())
                    .quantidade(dto.getQuantidade())
                    .tipo(dto.getTipo())
                    .ordemDeProducaoId(ordem.getId())
                    .build(),
                ordem
            ));
        }

        OrdemDeProducao savedOrdem = ordemDeProducaoRepository.save(ordem);

        registrarSaidaLote(lotePrincipal, consumoTotalMetros, "Consumido pela Ordem de Produção #" + savedOrdem.getId());
        registrarEntradaProduto(produto, requestDTO.getQuantidadeProduzida(), "Produzido via Ordem de Produção #" + savedOrdem.getId());
        distribuirEstoqueParaCanal(savedOrdem.getProduto().getId(), requestDTO.getCanalVendaDestinoId(), requestDTO.getQuantidadeProduzida());

        return ordemDeProducaoMapper.toDto(savedOrdem);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Orquestra as seguintes etapas:
     * <ol>
     *     <li>Valida se o produto é compatível com produção por consumo direto.</li>
     *     <li>Verifica se os lotes de matéria-prima especificados existem.</li>
     *     <li>Calcula o consumo total necessário e valida se o saldo dos lotes é suficiente.</li>
     *     <li>Cria e salva a Ordem de Produção.</li>
     *     <li>Registra a saída no estoque dos lotes de matéria-prima, consumindo-os na ordem em que foram fornecidos.</li>
     *     <li>Registra a entrada no estoque do produto acabado.</li>
     *     <li>Distribui o novo estoque de produto para um canal de venda, se especificado.</li>
     * </ol>
     *
     * @throws RegraNegocioException se o produto não for para consumo direto, se algum lote for inválido ou se o saldo de matéria-prima for insuficiente.
     */
    @Override
    @Transactional
    public OrdemDeProducaoResponseDTO criarOrdemDeConsumoDireto(OrdemDeConsumoDiretoRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (isDirectConsumptionUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
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

        // Conversão do DTO específico para OrdemDeProducaoRequestDTO
        OrdemDeProducaoRequestDTO ordemRequestDTO = OrdemDeProducaoRequestDTO.builder()
            .produtoId(produto.getId())
            .lotesConsumidosIds(new HashSet<>(requestDTO.getLotesConsumidosIds()))
            .canalVendaDestinoId(requestDTO.getCanalVendaDestinoId())
            .quantidadeProduzida(requestDTO.getQuantidadeProduzida())
            .motivo(requestDTO.getMotivo())
            .build();

        OrdemDeProducao ordem = OrdemDeProducao.from(ordemRequestDTO, produto, lotesConsumidos, null);
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

    /**
     * {@inheritDoc}
     * <p>
     * <b>Atenção:</b> Esta operação realiza uma exclusão física (hard delete) do registro da ordem.
     * As movimentações de estoque (entrada de produto e saída de matéria-prima) associadas
     * a esta ordem <em>não</em> são revertidas automaticamente.
     *
     * @throws RegraNegocioException se a ordem de produção com o ID especificado não for encontrada.
     */
    @Override
    @Transactional
    public void excluir(Long id) {
        if (!ordemDeProducaoRepository.existsById(id)) {
            throw new RegraNegocioException("Ordem de Produção não encontrada com ID: " + id);
        }
        ordemDeProducaoRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     *
     * @throws RegraNegocioException se a ordem de produção com o ID especificado não for encontrada.
     */
    @Override
    public OrdemDeProducaoResponseDTO buscarPorId(Long id) {
        return ordemDeProducaoRepository.findById(id)
                .map(ordemDeProducaoMapper::toDto)
                .orElseThrow(() -> new RegraNegocioException("Ordem de Produção não encontrada com ID: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrdemDeProducaoResponseDTO> listarTodas() {
        return ordemDeProducaoRepository.findAll().stream()
                .map(ordemDeProducaoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * @throws RegraNegocioException se o produto não for de um tipo geométrico ou se não houver lotes com estoque para simulação.
     */
    @Override
    public SimulacaoCorteResponseDTO simularCorte(SimulacaoCorteRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (isGeometricUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
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

    /**
     * {@inheritDoc}
     *
     * @throws RegraNegocioException se o produto for de um tipo geométrico, devendo-se usar o simulador de corte.
     */
    @Override
    public SimulacaoConsumoDiretoResponseDTO simularConsumoDireto(SimulacaoConsumoDiretoRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (isDirectConsumptionUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new RegraNegocioException("Este produto utiliza uma matéria-prima geométrica. Utilize o simulador de corte.");
        }

        BigDecimal consumoTotalEstimado = new BigDecimal(produto.getUnidadesPorProduto() * requestDTO.getQuantidade());
        return SimulacaoConsumoDiretoResponseDTO.builder()
                .consumoTotalEstimado(consumoTotalEstimado)
                .unidadeDeConsumo(produto.getTipoMateriaPrima().getUnidadeDeConsumo())
                .build();
    }

    /**
     * Gera a lista de cortes (produtos e retalhos) com base nos parâmetros de otimização.
     * Simula o corte linha a linha, agrupando os retalhos laterais contíguos para formar
     * lotes de sobra maiores e mais aproveitáveis.
     *
     * @param parametros Os parâmetros de corte calculados pelo {@link CorteCalculatorService}.
     * @param lotePrincipal O lote de matéria-prima original de onde o material está sendo cortado.
     * @return Uma lista de {@link CorteRealizadoResponseDTO} detalhando cada peça cortada (produto ou retalho).
     */
    private List<CorteRealizadoResponseDTO> gerarCortesRealizadosDinamico(ParametrosCorte parametros, LoteMateriaPrima lotePrincipal) {
        List<CorteRealizadoResponseDTO> cortesRealizados = new ArrayList<>();
        int produtosRestantes = parametros.quantidade();

        BigDecimal retalhoLarguraAcumulado = null;
        BigDecimal retalhoComprimentoAcumulado = BigDecimal.ZERO;

        while (produtosRestantes > 0) {
            int produtosNestaLinha = Math.min(parametros.produtosPorLinha(), produtosRestantes);
            if (produtosNestaLinha <= 0) break;

            cortesRealizados.add(criarCorteProduto(parametros.larguraProduto(), parametros.comprimentoProduto(), produtosNestaLinha));

            BigDecimal larguraProdutosOcupada = parametros.larguraProduto().multiply(new BigDecimal(produtosNestaLinha));
            BigDecimal larguraRetalhoLinha = parametros.larguraUtilCm().subtract(larguraProdutosOcupada);
            BigDecimal comprimentoLinha = parametros.comprimentoProduto();

            if (larguraRetalhoLinha.compareTo(BigDecimal.ZERO) > 0) {
                // Se a sobra atual tem a mesma largura da que estamos a acumular
                if (retalhoLarguraAcumulado != null && larguraRetalhoLinha.compareTo(retalhoLarguraAcumulado) == 0) {
                    // Apenas aumenta o comprimento do retalho acumulado
                    retalhoComprimentoAcumulado = retalhoComprimentoAcumulado.add(comprimentoLinha);
                } else {
                    // Se há um retalho acumulado de uma largura diferente, guarda-o primeiro
                    if (retalhoLarguraAcumulado != null && retalhoComprimentoAcumulado.compareTo(BigDecimal.ZERO) > 0) {
                        cortesRealizados.add(criarCorteRetalho(retalhoLarguraAcumulado, retalhoComprimentoAcumulado));
                        criarLoteDeRetalho(lotePrincipal, retalhoLarguraAcumulado, retalhoComprimentoAcumulado.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    }
                    // Inicia uma nova acumulação com a sobra da linha atual
                    retalhoLarguraAcumulado = larguraRetalhoLinha;
                    retalhoComprimentoAcumulado = comprimentoLinha;
                }
            } else { // Se esta linha não gerou retalho lateral
                // Se havia um retalho a ser acumulado, a sequência foi quebrada. Guarda-o.
                if (retalhoLarguraAcumulado != null && retalhoComprimentoAcumulado.compareTo(BigDecimal.ZERO) > 0) {
                    cortesRealizados.add(criarCorteRetalho(retalhoLarguraAcumulado, retalhoComprimentoAcumulado));
                    criarLoteDeRetalho(lotePrincipal, retalhoLarguraAcumulado, retalhoComprimentoAcumulado.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    // Reseta os acumuladores
                    retalhoLarguraAcumulado = null;
                    retalhoComprimentoAcumulado = BigDecimal.ZERO;
                }
            }
            produtosRestantes -= produtosNestaLinha;
        }

        // Garante que o último retalho acumulado (se houver) é guardado no final do processo
        if (retalhoLarguraAcumulado != null && retalhoComprimentoAcumulado.compareTo(BigDecimal.ZERO) > 0) {
            cortesRealizados.add(criarCorteRetalho(retalhoLarguraAcumulado, retalhoComprimentoAcumulado));
            criarLoteDeRetalho(lotePrincipal, retalhoLarguraAcumulado, retalhoComprimentoAcumulado.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        }

        return cortesRealizados;
    }

    /**
     * Cria um DTO para um corte de produto.
     */
    private CorteRealizadoResponseDTO criarCorteProduto(BigDecimal larguraProduto, BigDecimal comprimentoProduto, int quantidade) {
        return CorteRealizadoResponseDTO.builder()
                .larguraCm(larguraProduto)
                .comprimentoCm(comprimentoProduto)
                .quantidade(quantidade)
                .tipo("PRODUTO")
                .build();
    }

    /**
     * Cria um DTO para um corte de retalho (sobra).
     */
    private CorteRealizadoResponseDTO criarCorteRetalho(BigDecimal largura, BigDecimal comprimento) {
        return CorteRealizadoResponseDTO.builder()
                .larguraCm(largura)
                .comprimentoCm(comprimento)
                .quantidade(1)
                .tipo("RETALHO")
                .build();
    }

    /**
     * Cria um novo lote de matéria-prima a partir de um retalho (sobra de corte).
     * O novo lote é vinculado ao lote de origem e recebe uma movimentação de entrada de estoque.
     *
     * @param lotePrincipal O lote que originou o retalho.
     * @param larguraSobraCm A largura do retalho em centímetros.
     * @param comprimentoMetros O comprimento do retalho em metros.
     */
    private void criarLoteDeRetalho(LoteMateriaPrima lotePrincipal, BigDecimal larguraSobraCm, BigDecimal comprimentoMetros) {
        if (larguraSobraCm.compareTo(BigDecimal.ZERO) <= 0) return;
        Map<String, Object> novosAtributos = Map.of("larguraMm", larguraSobraCm.multiply(new BigDecimal("10")).intValue());
        LoteMateriaPrima loteRetalho = LoteMateriaPrima.builder()
                .tipoMateriaPrima(lotePrincipal.getTipoMateriaPrima())
                .unidadeDeEstoque(lotePrincipal.getUnidadeDeEstoque())
                .atributos(novosAtributos)
                .loteDeOrigem(lotePrincipal)
                .build();
        MovimentacaoRequestDTO entradaRetalhoDTO = com.dcriar.api.dto.request.stock.MovimentacaoRequestDTO.builder()
                .tipo(TipoMovimentacao.ENTRADA_SOBRA)
                .quantidade(comprimentoMetros)
                .motivo("Retalho gerado pela Ordem de Produção a partir do Lote ID: " + lotePrincipal.getId())
                .build();
        MovimentacaoEstoqueLote entradaRetalho = MovimentacaoEstoqueLote.from(entradaRetalhoDTO, loteRetalho);
        loteRetalho.getMovimentacoes().add(entradaRetalho);
        loteMateriaPrimaRepository.save(loteRetalho);
    }

    private void registrarSaidaLote(LoteMateriaPrima lote, BigDecimal quantidade, String motivo) {
        MovimentacaoRequestDTO saidaDTO = com.dcriar.api.dto.request.stock.MovimentacaoRequestDTO.builder()
                .tipo(com.dcriar.domain.stock.entity.enums.TipoMovimentacao.SAIDA_PRODUCAO)
                .quantidade(quantidade.negate())
                .motivo(motivo)
                .build();
        MovimentacaoEstoqueLote saida = MovimentacaoEstoqueLote.from(saidaDTO, lote);
        movimentacaoEstoqueLoteRepository.save(saida);
    }

    /**
     * Distribui a quantidade produzida de um produto para um canal de venda específico.
     * Invoca o {@link EstoqueProdutoService} para realizar o ajuste de estoque no canal.
     *
     * @param produtoId O ID do produto produzido.
     * @param canalVendaId O ID do canal de venda de destino. Se nulo, nenhuma ação é tomada.
     * @param quantidade A quantidade a ser distribuída.
     */
    public void distribuirEstoqueParaCanal(Long produtoId, Long canalVendaId, Integer quantidade) {
        if (canalVendaId != null && quantidade != null && quantidade > 0) {
            AjusteEstoqueRequestDTO ajusteDTO = AjusteEstoqueRequestDTO.builder()
                    .produtoId(produtoId)
                    .canalVendaId(canalVendaId)
                    .quantidade(quantidade)
                    .build();
            estoqueProdutoService.ajustarEstoque(ajusteDTO);
        }
    }

    /**
     * Registra uma movimentação de entrada no estoque físico (mestre) de um produto.
     *
     * @param produto O produto que teve seu estoque aumentado.
     * @param quantidade A quantidade produzida.
     * @param motivo A descrição da origem da movimentação.
     */
    public void registrarEntradaProduto(Produto produto, int quantidade, String motivo) {
        MovimentacaoEstoqueProdutoRequestDTO movimentacaoDTO = MovimentacaoEstoqueProdutoRequestDTO.builder()
                .produtoId(produto.getId())
                .tipo(TipoMovimentacaoProduto.ENTRADA_PRODUCAO.name())
                .quantidade(quantidade)
                .motivo(motivo)
                .build();
        MovimentacaoEstoqueProduto entrada = MovimentacaoEstoqueProduto.from(movimentacaoDTO, produto);
        movimentacaoEstoqueProdutoRepository.save(entrada);
    }

    /**
     * Valida se o saldo de um lote de matéria-prima é suficiente para o consumo de um corte.
     *
     * @param lote O lote a ser verificado.
     * @param consumoEmMetros A quantidade necessária em metros.
     * @throws RegraNegocioException se o saldo for insuficiente.
     */
    private void validarSaldoLoteCorte(LoteMateriaPrima lote, BigDecimal consumoEmMetros) {
        BigDecimal saldoAtual = movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
        if (consumoEmMetros.compareTo(saldoAtual) > 0) {
            throw new RegraNegocioException(
                    String.format("Saldo insuficiente para o corte. Necessário: %.2f m, Disponível: %.2f m", consumoEmMetros, saldoAtual)
            );
        }
    }

    /**
     * Busca uma entidade {@link Produto} pelo seu ID.
     *
     * @param id O ID do produto.
     * @return A entidade {@link Produto} encontrada.
     * @throws ProdutoNotFoundException se o produto não for encontrado.
     */
    private Produto findProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    /**
     * Busca uma entidade {@link LoteMateriaPrima} pelo seu ID.
     *
     * @param id O ID do lote.
     * @return A entidade {@link LoteMateriaPrima} encontrada.
     * @throws LoteMateriaPrimaNotFoundException se o lote não for encontrado.
     */
    private LoteMateriaPrima findLoteById(Long id) {
        return loteMateriaPrimaRepository.findById(id)
                .orElseThrow(() -> new LoteMateriaPrimaNotFoundException(id));
    }

    /**
     * Verifica se a unidade de medida é do tipo geométrico (linear ou área).
     */
    private boolean isGeometricUnit(UnidadeDeMedida unidade) {
        return unidade != UnidadeDeMedida.METRO_LINEAR &&
                unidade != UnidadeDeMedida.CENTIMETRO_LINEAR &&
                unidade != UnidadeDeMedida.METRO_QUADRADO &&
                unidade != UnidadeDeMedida.CENTIMETRO_QUADRADO;
    }

    /**
     * Verifica se a unidade de medida é do tipo de consumo direto (volume, massa, unidade).
     */
    private boolean isDirectConsumptionUnit(UnidadeDeMedida unidade) {
        return unidade != UnidadeDeMedida.LITRO &&
                unidade != UnidadeDeMedida.MILILITRO &&
                unidade != UnidadeDeMedida.QUILOGRAMA &&
                unidade != UnidadeDeMedida.GRAMA &&
                unidade != UnidadeDeMedida.UNIDADE;
    }
}


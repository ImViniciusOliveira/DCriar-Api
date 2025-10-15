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
import com.dcriar.domain.product.entity.MovimentacaoEstoqueProduto;
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
import com.dcriar.exception.custom.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Implementação do serviço para gerir Ordens de Produção.
 * <p>
 * Esta classe orquestra a criação de ordens por corte (com otimização de layout)
 * e por consumo direto, gerenciando a movimentação de estoque de matéria-prima e produtos acabados.
 * <p>
 * <b>Responsabilidades Principais:</b>
 * <ul>
 *     <li>Criar ordens de produção, validando a compatibilidade do tipo de produção.</li>
 *     <li>Orquestrar a baixa no estoque de matéria-prima.</li>
 *     <li>Orquestrar a entrada no estoque de produtos acabados.</li>
 *     <li>Gerar novos lotes de matéria-prima a partir de sobras (retalhos) em ordens de corte.</li>
 *     <li>Distribuir opcionalmente o estoque produzido para canais de venda.</li>
 * </ul>
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
     * Cria uma Ordem de Produção baseada em corte de matéria-prima.
     * <p>
     * <b>Processo de Orquestração:</b>
     * <ol>
     *     <li>Valida se o produto é compatível com produção por corte.</li>
     *     <li>Calcula os parâmetros de corte (se modo automático) ou usa os dados manuais.</li>
     *     <li>Valida se há saldo suficiente no lote de matéria-prima.</li>
     *     <li>Cria e salva a Ordem de Produção com os detalhes do processo.</li>
     *     <li><b>Efeito Colateral:</b> Se o corte gerar sobras (retalhos), novos lotes de matéria-prima são criados automaticamente.</li>
     *     <li>Registra a saída no estoque do lote de matéria-prima.</li>
     *     <li>Registra a entrada no estoque do produto acabado.</li>
     *     <li><b>Distribuição Opcional:</b> Se um {@code canalVendaDestinoId} for fornecido, a quantidade produzida é automaticamente distribuída para o estoque daquele canal.</li>
     * </ol>
     *
     * @param requestDTO O DTO com os dados da ordem de corte.
     * @return O DTO de resposta da ordem de produção criada.
     * @throws TipoProducaoIncompativelException se o produto não for compatível com produção por corte.
     * @throws LotePrincipalNaoEspecificadoException se o lote principal não for especificado.
     * @throws DimensoesManuaisInvalidasException se o modo de cálculo for manual e as dimensões não forem fornecidas.
     * @throws SaldoMateriaPrimaInsuficienteException se o saldo do lote de matéria-prima for insuficiente.
     */
    @Override
    @Transactional
    public OrdemDeProducaoResponseDTO criarOrdemDeCorte(OrdemDeCorteRequestDTO requestDTO) {
        // 1. Validações iniciais e busca de entidades principais.
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (isGeometricUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new TipoProducaoIncompativelException("Este produto não pode ser produzido por corte. Utilize o endpoint de consumo direto.");
        }

        if (requestDTO.getLotePrincipalId() == null) {
            throw new LotePrincipalNaoEspecificadoException("A produção por corte exige a especificação de um 'lotePrincipalId'.");
        }
        LoteMateriaPrima lotePrincipal = findLoteById(requestDTO.getLotePrincipalId());

        BigDecimal consumoTotalMetros;
        BigDecimal comprimentoFinalCm;
        List<CorteRealizadoResponseDTO> cortesRealizadosDTOs;
        BigDecimal larguraFinalCm;
        ParametrosCorte parametros = null;

        // 2. Determina os parâmetros de corte (manual ou automático).
        if (requestDTO.getModoCalculo() == ModoCalculo.MANUAL) {
            if (requestDTO.getLarguraFinalCm() == null || requestDTO.getComprimentoFinalCm() == null) {
                throw new DimensoesManuaisInvalidasException("Para o modo MANUAL, as dimensões finais (largura e comprimento) são obrigatórias.");
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

        // 3. Valida se o lote principal tem saldo suficiente.
        validarSaldoLoteCorte(lotePrincipal, consumoTotalMetros);

        // 4. Cria e persiste a Ordem de Produção e seus cortes.
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

        // 5. Orquestra as movimentações de estoque.
        registrarSaidaLote(lotePrincipal, consumoTotalMetros, "Consumido pela Ordem de Produção #" + savedOrdem.getId());
        registrarEntradaProduto(produto, requestDTO.getQuantidadeProduzida(), "Produzido via Ordem de Produção #" + savedOrdem.getId());
        distribuirEstoqueParaCanal(savedOrdem.getProduto().getId(), requestDTO.getCanalVendaDestinoId(), requestDTO.getQuantidadeProduzida());

        return ordemDeProducaoMapper.toDto(savedOrdem);
    }

    /**
     * Cria uma Ordem de Produção baseada em consumo direto de matéria-prima.
     * <p>
     * <b>Processo de Orquestração:</b>
     * <ol>
     *     <li>Valida se o produto é compatível com produção por consumo direto.</li>
     *     <li>Verifica se os lotes de matéria-prima especificados existem.</li>
     *     <li>Calcula o consumo total necessário e valida se o saldo somado dos lotes é suficiente.</li>
     *     <li>Cria e salva a Ordem de Produção.</li>
     *     <li>Registra a saída no estoque dos lotes de matéria-prima, consumindo-os sequencialmente até que a necessidade total seja atendida.</li>
     *     <li>Registra a entrada no estoque do produto acabado.</li>
     *     <li><b>Distribuição Opcional:</b> Se um {@code canalVendaDestinoId} for fornecido, a quantidade produzida é automaticamente distribuída para o estoque daquele canal.</li>
     * </ol>
     *
     * @param requestDTO O DTO com os dados da ordem de consumo direto.
     * @return O DTO de resposta da ordem de produção criada.
     * @throws TipoProducaoIncompativelException se o produto não for para consumo direto.
     * @throws RegraNegocioException se algum dos IDs de lote fornecidos for inválido.
     * @throws SaldoMateriaPrimaInsuficienteException se o saldo combinado dos lotes de matéria-prima for insuficiente.
     */
    @Override
    @Transactional
    public OrdemDeProducaoResponseDTO criarOrdemDeConsumoDireto(OrdemDeConsumoDiretoRequestDTO requestDTO) {
        // 1. Validações iniciais e busca de entidades.
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (isDirectConsumptionUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new TipoProducaoIncompativelException("Este produto não pode ser produzido por consumo direto. Utilize o endpoint de corte.");
        }

        Set<LoteMateriaPrima> lotesConsumidos = new HashSet<>(loteMateriaPrimaRepository.findAllById(requestDTO.getLotesConsumidosIds()));
        if (lotesConsumidos.size() != requestDTO.getLotesConsumidosIds().size()) {
            throw new RegraNegocioException("Um ou mais IDs de lote fornecidos são inválidos.");
        }

        // 2. Valida se o saldo total dos lotes é suficiente.
        BigDecimal consumoTotalNecessario = new BigDecimal(produto.getUnidadesPorProduto() * requestDTO.getQuantidadeProduzida());
        BigDecimal saldoTotalDisponivel = lotesConsumidos.stream()
                .map(movimentacaoEstoqueLoteRepository::findSaldoByLote)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (saldoTotalDisponivel.compareTo(consumoTotalNecessario) < 0) {
            throw new SaldoMateriaPrimaInsuficienteException(consumoTotalNecessario, saldoTotalDisponivel);
        }

        // 3. Cria e persiste a Ordem de Produção.
        OrdemDeProducaoRequestDTO ordemRequestDTO = OrdemDeProducaoRequestDTO.builder()
            .produtoId(produto.getId())
            .lotesConsumidosIds(new HashSet<>(requestDTO.getLotesConsumidosIds()))
            .canalVendaDestinoId(requestDTO.getCanalVendaDestinoId())
            .quantidadeProduzida(requestDTO.getQuantidadeProduzida())
            .motivo(requestDTO.getMotivo())
            .build();

        OrdemDeProducao ordem = OrdemDeProducao.from(ordemRequestDTO, produto, lotesConsumidos, null);
        OrdemDeProducao savedOrdem = ordemDeProducaoRepository.save(ordem);

        // 4. Orquestra as movimentações de estoque.
        // Consome dos lotes sequencialmente até atingir o necessário.
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
     * Exclui uma Ordem de Produção pelo seu ID.
     * <p>
     * <b>Atenção:</b> Esta operação realiza uma exclusão física (hard delete) do registro da ordem
     * e de seus cortes associados. As movimentações de estoque (entrada de produto e saída de matéria-prima)
     * geradas por esta ordem <em>não</em> são revertidas automaticamente, o que pode levar a
     * inconsistências nos saldos de estoque.
     *
     * @param id O ID da ordem de produção a ser excluída.
     * @throws OrdemDeProducaoNaoEncontradaException se a ordem de produção com o ID especificado não for encontrada.
     */
    @Override
    @Transactional
    public void excluir(Long id) {
        if (!ordemDeProducaoRepository.existsById(id)) {
            throw new OrdemDeProducaoNaoEncontradaException(id);
        }
        ordemDeProducaoRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OrdemDeProducaoNaoEncontradaException se a ordem de produção com o ID especificado não for encontrada.
     */
    @Override
    public OrdemDeProducaoResponseDTO buscarPorId(Long id) {
        return ordemDeProducaoRepository.findById(id)
                .map(ordemDeProducaoMapper::toDto)
                .orElseThrow(() -> new OrdemDeProducaoNaoEncontradaException(id));
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
     * Simula uma ordem de produção por corte para estimar o consumo de matéria-prima.
     * <p>
     * <b>Regra de Negócio:</b> Para realizar a simulação, o serviço busca o <strong>primeiro</strong>
     * lote de matéria-prima compatível que possua estoque disponível. A simulação será baseada
     * nos atributos (ex: largura) deste lote específico.
     *
     * @param requestDTO O DTO com os dados para a simulação.
     * @return Um DTO com os resultados da simulação.
     * @throws TipoProducaoIncompativelException se o produto não for de um tipo geométrico.
     * @throws NenhumLoteComEstoqueException se não houver lotes com estoque para simulação.
     */
    @Override
    public SimulacaoCorteResponseDTO simularCorte(SimulacaoCorteRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (isGeometricUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new TipoProducaoIncompativelException("Este produto não utiliza uma matéria-prima geométrica para simulação de corte.");
        }

        // Busca o primeiro lote compatível com estoque para usar como base para a simulação.
        LoteMateriaPrima loteParaSimulacao = loteMateriaPrimaRepository.findAllByTipoMateriaPrima(produto.getTipoMateriaPrima()).stream()
                .filter(lote -> movimentacaoEstoqueLoteRepository.findSaldoByLote(lote).compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElseThrow(() -> new NenhumLoteComEstoqueException("Não há lotes de matéria-prima com estoque disponível para este produto."));


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
     * @throws TipoProducaoIncompativelException se o produto for de um tipo geométrico, devendo-se usar o simulador de corte.
     */
    @Override
    public SimulacaoConsumoDiretoResponseDTO simularConsumoDireto(SimulacaoConsumoDiretoRequestDTO requestDTO) {
        Produto produto = findProdutoById(requestDTO.getProdutoId());
        if (isDirectConsumptionUnit(produto.getTipoMateriaPrima().getUnidadeDeConsumo())) {
            throw new TipoProducaoIncompativelException("Este produto utiliza uma matéria-prima geométrica. Utilize o simulador de corte.");
        }

        BigDecimal consumoTotalEstimado = new BigDecimal(produto.getUnidadesPorProduto() * requestDTO.getQuantidade());
        return SimulacaoConsumoDiretoResponseDTO.builder()
                .consumoTotalEstimado(consumoTotalEstimado)
                .unidadeDeConsumo(produto.getTipoMateriaPrima().getUnidadeDeConsumo())
                .build();
    }

    /**
     * Gera a lista de cortes (produtos e retalhos) com base nos parâmetros de otimização.
     * <p>
     * Este método simula o corte linha a linha, agrupando os retalhos laterais contíguos para formar
     * lotes de sobra maiores e mais aproveitáveis.
     * <p>
     * <b>Efeito Colateral Importante:</b> Para cada retalho gerado, este método invoca {@link #criarLoteDeRetalho(LoteMateriaPrima, BigDecimal, BigDecimal)},
     * que cria uma nova entidade {@link LoteMateriaPrima} no banco de dados.
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

        // Itera linha por linha do plano de corte.
        while (produtosRestantes > 0) {
            int produtosNestaLinha = Math.min(parametros.produtosPorLinha(), produtosRestantes);
            if (produtosNestaLinha <= 0) break;

            // Adiciona o corte do produto principal.
            cortesRealizados.add(criarCorteProduto(parametros.larguraProduto(), parametros.comprimentoProduto(), produtosNestaLinha));

            // Calcula a largura da sobra (retalho) nesta linha.
            BigDecimal larguraProdutosOcupada = parametros.larguraProduto().multiply(new BigDecimal(produtosNestaLinha));
            BigDecimal larguraRetalhoLinha = parametros.larguraUtilCm().subtract(larguraProdutosOcupada);
            BigDecimal comprimentoLinha = parametros.comprimentoProduto();

            if (larguraRetalhoLinha.compareTo(BigDecimal.ZERO) > 0) {
                // Lógica para agrupar retalhos contíguos de mesma largura.
                if (retalhoLarguraAcumulado != null && larguraRetalhoLinha.compareTo(retalhoLarguraAcumulado) == 0) {
                    // Se a sobra atual tem a mesma largura da anterior, apenas aumenta o comprimento acumulado.
                    retalhoComprimentoAcumulado = retalhoComprimentoAcumulado.add(comprimentoLinha);
                } else {
                    // Se a largura mudou, salva o retalho acumulado anteriormente (se existir).
                    if (retalhoLarguraAcumulado != null && retalhoComprimentoAcumulado.compareTo(BigDecimal.ZERO) > 0) {
                        cortesRealizados.add(criarCorteRetalho(retalhoLarguraAcumulado, retalhoComprimentoAcumulado));
                        criarLoteDeRetalho(lotePrincipal, retalhoLarguraAcumulado, retalhoComprimentoAcumulado.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    }
                    // Inicia uma nova acumulação com o retalho da linha atual.
                    retalhoLarguraAcumulado = larguraRetalhoLinha;
                    retalhoComprimentoAcumulado = comprimentoLinha;
                }
            } else { // Se esta linha não gerou retalho lateral.
                // Se havia um retalho sendo acumulado, a sequência foi quebrada. Salva-o.
                if (retalhoLarguraAcumulado != null && retalhoComprimentoAcumulado.compareTo(BigDecimal.ZERO) > 0) {
                    cortesRealizados.add(criarCorteRetalho(retalhoLarguraAcumulado, retalhoComprimentoAcumulado));
                    criarLoteDeRetalho(lotePrincipal, retalhoLarguraAcumulado, retalhoComprimentoAcumulado.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    // Reseta os acumuladores.
                    retalhoLarguraAcumulado = null;
                    retalhoComprimentoAcumulado = BigDecimal.ZERO;
                }
            }
            produtosRestantes -= produtosNestaLinha;
        }

        // Garante que o último retalho acumulado (se houver) seja salvo no final do processo.
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

    /**
     * Orquestra a baixa de estoque de um lote de matéria-prima.
     */
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
     * Orquestra a distribuição da quantidade produzida para o estoque de um canal de venda.
     * Se o ID do canal for nulo, nenhuma ação é tomada.
     *
     * @param produtoId O ID do produto produzido.
     * @param canalVendaId O ID do canal de venda de destino (pode ser nulo).
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
     * Orquestra a entrada de um produto acabado no estoque mestre.
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
     * @throws SaldoMateriaPrimaInsuficienteException se o saldo for insuficiente.
     */
    private void validarSaldoLoteCorte(LoteMateriaPrima lote, BigDecimal consumoEmMetros) {
        BigDecimal saldoAtual = movimentacaoEstoqueLoteRepository.findSaldoByLote(lote);
        if (consumoEmMetros.compareTo(saldoAtual) > 0) {
            throw new SaldoMateriaPrimaInsuficienteException(consumoEmMetros, saldoAtual);
        }
    }

    /**
     * Busca uma entidade {@link Produto} pelo seu ID.
     *
     * @param id O ID do produto.
     * @return A entidade {@link Produto} encontrada.
     * @throws ProdutoNaoEncontradoException se o produto não for encontrado.
     */
    private Produto findProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
    }

    /**
     * Busca uma entidade {@link LoteMateriaPrima} pelo seu ID.
     *
     * @param id O ID do lote.
     * @return A entidade {@link LoteMateriaPrima} encontrada.
     * @throws LoteMateriaPrimaNaoEncontradoException se o lote não for encontrado.
     */
    private LoteMateriaPrima findLoteById(Long id) {
        return loteMateriaPrimaRepository.findById(id)
                .orElseThrow(() -> new LoteMateriaPrimaNaoEncontradoException(id));
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

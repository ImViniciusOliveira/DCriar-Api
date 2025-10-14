package com.dcriar.exception.handler;

import com.dcriar.api.dto.response.ErrorResponseDTO;
import com.dcriar.exception.custom.*;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Handler de exceções global para toda a aplicação.
 * <p>
 * Anotado com {@link ControllerAdvice}, ele intercepta exceções lançadas pelos controllers
 * e as converte em respostas HTTP padronizadas no formato {@link ErrorResponseDTO}.
 * Isso garante que a API sempre retorne respostas de erro consistentes e estruturadas.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    //region Exceções de Domínio e Negócio

    /**
     * Trata exceções para entidades não encontradas (HTTP 404 Not Found).
     * Intercepta {@link ProdutoNotFoundException}, {@link CanalVendaNotFoundException},
     * {@link LoteMateriaPrimaNotFoundException}, {@link TipoMateriaPrimaNotFoundException},
     * e {@link SaleNotFoundException}.
     *
     * @param ex A exceção de "não encontrado" lançada.
     * @return Um {@link ResponseEntity} contendo um {@link ErrorResponseDTO} com status 404.
     */
    @ExceptionHandler({ProdutoNotFoundException.class, CanalVendaNotFoundException.class, LoteMateriaPrimaNotFoundException.class, TipoMateriaPrimaNotFoundException.class, SaleNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleNotFoundExceptions(RuntimeException ex) {
        String key = "id";
        String value = "N/A";

        if (ex instanceof ProdutoNotFoundException e) { key = "produtoId"; value = String.valueOf(e.getId()); }
        else if (ex instanceof CanalVendaNotFoundException e) { key = "canalVendaId"; value = String.valueOf(e.getId()); }
        else if (ex instanceof LoteMateriaPrimaNotFoundException e) { key = "loteId"; value = String.valueOf(e.getId()); }
        else if (ex instanceof TipoMateriaPrimaNotFoundException e) { key = "materiaPrimaId"; value = String.valueOf(e.getMateriaPrimaId()); }
        else if (ex instanceof SaleNotFoundException e) { key = "saleId"; value = String.valueOf(e.getSaleId()); }

        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, Map.of(key, value));
    }

    /**
     * Trata exceções de violação de regras de negócio genéricas (HTTP 400 Bad Request).
     * Intercepta {@link RegraNegocioException}, {@link EstoqueRegraNegocioException},
     * e {@link PrecoVarejoNaoDefinidoException}.
     *
     * @param ex A exceção de regra de negócio lançada.
     * @return Um {@link ResponseEntity} contendo um {@link ErrorResponseDTO} com status 400.
     */
    @ExceptionHandler({RegraNegocioException.class, EstoqueRegraNegocioException.class, PrecoVarejoNaoDefinidoException.class})
    public ResponseEntity<ErrorResponseDTO> handleBusinessRuleExceptions(RuntimeException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, Map.of("info", ex.getMessage()));
    }

    /**
     * Trata exceções de conflito, como criação de recurso duplicado ou recurso em uso (HTTP 409 Conflict).
     * Intercepta {@link TipoMateriaPrimaAlreadyExistsException}, {@link ProdutoEmUsoException},
     * e {@link TipoMateriaPrimaEmUsoException}.
     *
     * @param ex A exceção de conflito lançada.
     * @return Um {@link ResponseEntity} contendo um {@link ErrorResponseDTO} com status 409.
     */
    @ExceptionHandler({TipoMateriaPrimaAlreadyExistsException.class, ProdutoEmUsoException.class, TipoMateriaPrimaEmUsoException.class})
    public ResponseEntity<ErrorResponseDTO> handleConflictExceptions(RuntimeException ex) {
        Map<String, String> details = new HashMap<>();
        if (ex instanceof TipoMateriaPrimaAlreadyExistsException e) { details.put("nome", e.getNome()); }
        else if (ex instanceof ProdutoEmUsoException e) { details.put("produtoId", String.valueOf(e.getProdutoId())); details.put("entidadesEmUso", e.getEntidadeIds().toString()); }
        else if (ex instanceof TipoMateriaPrimaEmUsoException e) { details.put("tipoMateriaPrimaId", String.valueOf(e.getTipoMateriaPrimaId())); details.put("lotesEmUso", e.getLoteIds().toString()); }

        return buildErrorResponse(ex, HttpStatus.CONFLICT, details);
    }

    /**
     * Trata exceções de validação de negócio com múltiplos campos (HTTP 400 Bad Request).
     * Intercepta {@link ProdutoInvalidoException}.
     *
     * @param ex A exceção {@link ProdutoInvalidoException} lançada.
     * @return Um {@link ResponseEntity} contendo um {@link ErrorResponseDTO} com status 400 e detalhes dos erros.
     */
    @ExceptionHandler(ProdutoInvalidoException.class)
    public ResponseEntity<ErrorResponseDTO> handleMultiFieldValidation(ProdutoInvalidoException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getErrors());
    }

    /**
     * Trata exceções de estoque insuficiente para uma operação (HTTP 400 Bad Request).
     * Intercepta {@link EstoqueInsuficienteParaMovimentacaoException} e {@link EstoqueInsuficienteCanalException}.
     *
     * @param ex A exceção de estoque insuficiente lançada.
     * @return Um {@link ResponseEntity} contendo um {@link ErrorResponseDTO} com status 400 e detalhes do estoque.
     */
    @ExceptionHandler({EstoqueInsuficienteParaMovimentacaoException.class, EstoqueInsuficienteCanalException.class})
    public ResponseEntity<ErrorResponseDTO> handleInsufficientStock(RuntimeException ex) {
        Map<String, String> details = new HashMap<>();
        if (ex instanceof EstoqueInsuficienteParaMovimentacaoException e) {
            details.put("loteId", String.valueOf(e.getLoteId()));
            details.put("quantidadeRequisitada", String.valueOf(Math.abs(e.getQuantidadeRequisitada())));
            details.put("saldoDisponivel", String.valueOf(e.getSaldoDisponivel()));
        } else if (ex instanceof EstoqueInsuficienteCanalException e) {
            details.put("produtoId", String.valueOf(e.getProdutoId()));
            details.put("canalVendaId", String.valueOf(e.getCanalVendaId()));
            details.put("quantidadeRequisitada", String.valueOf(Math.abs(e.getQuantidadeRequisitada())));
            details.put("estoqueAtual", String.valueOf(e.getEstoqueAtual()));
        }
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, details);
    }

    //endregion

    //region Exceções do Spring Framework

    /**
     * Trata erros de validação de argumentos de método ({@code @Valid}) (HTTP 400 Bad Request).
     * Converte os erros de validação do Spring em um mapa de detalhes para o {@link ErrorResponseDTO}.
     *
     * @param ex A exceção {@link MethodArgumentNotValidException} lançada.
     * @return Um {@link ResponseEntity} contendo um {@link ErrorResponseDTO} com status 400 e detalhes dos erros de campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors().forEach(error -> errors.put(error.getObjectName(), error.getDefaultMessage()));
        return buildErrorResponse("Erros de validação encontrados", HttpStatus.BAD_REQUEST, errors);
    }

    /**
     * Trata erros de desserialização de JSON ou requisições com corpo ilegível (HTTP 400 Bad Request).
     * Fornece detalhes sobre o campo que causou o erro de formato, se disponível.
     *
     * @param ex A exceção {@link HttpMessageNotReadableException} lançada.
     * @return Um {@link ResponseEntity} contendo um {@link ErrorResponseDTO} com status 400.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMalformedJson(HttpMessageNotReadableException ex) {
        String msg = "JSON malformado ou sintaxe inválida na requisição.";
        String detalhe;
        if (ex.getMostSpecificCause() instanceof InvalidFormatException invalidFormat) {
            String campo = invalidFormat.getPath().stream().map(JsonMappingException.Reference::getFieldName).collect(Collectors.joining("."));
            detalhe = String.format("Campo '%s' recebeu valor inválido: '%s'.", campo, invalidFormat.getValue());
        } else {
            ex.getMostSpecificCause();
            detalhe = ex.getMostSpecificCause().getMessage();
        }
        log.warn("JSON inválido: {}", detalhe);
        return buildErrorResponse(msg, HttpStatus.BAD_REQUEST, Map.of("erro", detalhe));
    }

    /**
     * Trata o uso de métodos HTTP não suportados por um endpoint (HTTP 405 Method Not Allowed).
     * Informa quais métodos HTTP são permitidos para o recurso.
     *
     * @param ex A exceção {@link HttpRequestMethodNotSupportedException} lançada.
     * @return Um {@link ResponseEntity} contendo um {@link ErrorResponseDTO} com status 405.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String msg = String.format("Método HTTP '%s' não permitido para este recurso.", ex.getMethod());
        String metodosPermitidos = Objects.requireNonNull(ex.getSupportedHttpMethods()).stream().map(HttpMethod::name).collect(Collectors.joining(", "));
        return buildErrorResponse(msg, HttpStatus.METHOD_NOT_ALLOWED, Map.of("metodosPermitidos", metodosPermitidos));
    }

    /**
     * Trata exceções internas relacionadas ao processo de mesclagem de JSON em operações PATCH.
     * Retorna HTTP 500, pois este é um erro inesperado do lado do servidor.
     *
     * @param ex A exceção {@link JsonMergeException} lançada.
     * @return Um {@link ResponseEntity} contendo um {@link ErrorResponseDTO} com status 500.
     */
    @ExceptionHandler(JsonMergeException.class)
    public ResponseEntity<ErrorResponseDTO> handleJsonMergeException(JsonMergeException ex) {
        log.error("Falha ao mesclar JSON para operação PATCH: ", ex);
        String msg = "Ocorreu um erro interno ao processar a atualização. A estrutura dos dados enviados pode ser inválida.";
        return buildErrorResponse(msg, HttpStatus.INTERNAL_SERVER_ERROR, Map.of("detalhe", ex.getMessage()));
    }

    /**
     * Handler genérico para qualquer outra exceção não tratada (HTTP 500 Internal Server Error).
     * Registra a exceção e retorna uma mensagem de erro genérica para o cliente.
     *
     * @param ex A exceção genérica capturada.
     * @return Um {@link ResponseEntity} contendo um {@link ErrorResponseDTO} com status 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        log.error("Erro inesperado: ", ex);
        String msg = "Ocorreu um erro inesperado. Tente novamente mais tarde.";
        return buildErrorResponse(msg, HttpStatus.INTERNAL_SERVER_ERROR, Map.of("exception", ex.getClass().getSimpleName()));
    }

    //endregion

    //region Métodos Auxiliares

    /**
     * Constrói uma resposta de erro padronizada a partir de uma mensagem e detalhes específicos.
     *
     * @param message A mensagem principal do erro.
     * @param status O status HTTP a ser retornado.
     * @param details Um mapa de detalhes adicionais do erro.
     * @return Um {@link ResponseEntity} contendo o {@link ErrorResponseDTO} e o status HTTP.
     */
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(String message, HttpStatus status, Map<String, String> details) {
        ErrorResponseDTO dto = new ErrorResponseDTO(Instant.now(), status.value(), status.getReasonPhrase(), message, details);
        return new ResponseEntity<>(dto, status);
    }

    /**
     * Constrói uma resposta de erro padronizada a partir de uma exceção e detalhes específicos.
     * Utiliza a mensagem da exceção como mensagem principal do erro.
     *
     * @param ex A exceção que foi capturada.
     * @param status O status HTTP a ser retornado.
     * @param details Um mapa de detalhes adicionais do erro.
     * @return Um {@link ResponseEntity} contendo o {@link ErrorResponseDTO} e o status HTTP.
     */
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(Exception ex, HttpStatus status, Map<String, String> details) {
        return buildErrorResponse(ex.getMessage(), status, details);
    }

    //endregion
}

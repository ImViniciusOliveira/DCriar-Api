package com.dcriar.exception.handler;

import com.dcriar.api.dto.response.ErrorDTO;
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
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleProdutoNotFound(ProdutoNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, Map.of("produtoId", ex.getId().toString()));
    }

    @ExceptionHandler(CanalVendaNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleCanalNotFound(CanalVendaNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, Map.of("canalVendaId", ex.getId().toString()));
    }

    @ExceptionHandler(LoteMateriaPrimaNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleLoteNotFound(LoteMateriaPrimaNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, Map.of("loteId", ex.getId().toString()));
    }

    @ExceptionHandler(TipoMateriaPrimaNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleTipoMateriaPrimaNotFound(TipoMateriaPrimaNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, Map.of("materiaPrimaId", ex.getMateriaPrimaId().toString()));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorDTO> handleRegraNegocio(RegraNegocioException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, Map.of("info", ex.getMessage()));
    }

    @ExceptionHandler(SaleNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleSaleNotFound(SaleNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, Map.of("saleId", ex.getSaleId().toString()));
    }

    @ExceptionHandler(PrecoVarejoNaoDefinidoException.class)
    public ResponseEntity<ErrorDTO> handlePrecoVarejoNaoDefinido(PrecoVarejoNaoDefinidoException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, Map.of("produtoId", ex.getProdutoId().toString()));
    }

    @ExceptionHandler(EstoqueRegraNegocioException.class)
    public ResponseEntity<ErrorDTO> handleEstoqueException(EstoqueRegraNegocioException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, Map.of("info", ex.getMessage()));
    }

    @ExceptionHandler(TipoMateriaPrimaAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleTipoMateriaPrimaAlreadyExists(TipoMateriaPrimaAlreadyExistsException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, Map.of("nome", ex.getNome()));
    }

    @ExceptionHandler(TipoMateriaPrimaEmUsoException.class)
    public ResponseEntity<ErrorDTO> handleTipoMateriaPrimaEmUso(TipoMateriaPrimaEmUsoException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, Map.of("info", ex.getMessage()));
    }

    @ExceptionHandler(ProdutoInvalidoException.class)
    public ResponseEntity<ErrorDTO> handleProdutoInvalido(ProdutoInvalidoException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, Map.copyOf(ex.getErrors()));
    }

    @ExceptionHandler(ProdutoValidationException.class)
    public ResponseEntity<ErrorDTO> handleProdutoValidation(ProdutoValidationException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getErrors());
    }

    @ExceptionHandler(ProdutoEmUsoException.class)
    public ResponseEntity<Map<String, String>> handleProdutoEmUso(ProdutoEmUsoException ex) {
        Map<String, String> response = Map.of("erro", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EstoqueInsuficienteParaMovimentacaoException.class)
    public ResponseEntity<ErrorDTO> handleEstoqueInsuficienteParaMovimentacao(EstoqueInsuficienteParaMovimentacaoException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, Map.of(
                "loteId", String.valueOf(ex.getLoteId()),
                "quantidadeRequisitada", String.valueOf(Math.abs(ex.getQuantidadeRequisitada())),
                "estoqueAtual", String.valueOf(ex.getSaldoDisponivel())
        ));
    }

    @ExceptionHandler(EstoqueInsuficienteCanalException.class)
    public ResponseEntity<ErrorDTO> handleEstoqueNegativoNoCanalException(EstoqueInsuficienteCanalException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, Map.of(
                "produtoId", ex.getProdutoId().toString(),
                "canalVendaId", ex.getCanalVendaId().toString(),
                "quantidadeRequisitada", String.valueOf(Math.abs(ex.getQuantidadeRequisitada())),
                "estoqueAtual", String.valueOf(ex.getEstoqueAtual())
        ));
    }

    // ============================
    // Exceções de validação
    // ============================

    // ## MÉTODO CORRIGIDO ##
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errors.put(error.getObjectName(), error.getDefaultMessage())
        );

        return buildErrorResponse("Erros de validação encontrados", HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(MultiValidationException.class)
    public ResponseEntity<ErrorDTO> handleMultiValidation(MultiValidationException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, ex.getErrors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> handleMalformedJson(HttpMessageNotReadableException ex) {
        String msg = "JSON malformado ou sintaxe inválida na requisição.";
        String detalhe;

        if (ex.getMostSpecificCause() instanceof InvalidFormatException invalidFormat) {
            Object[] constants = invalidFormat.getTargetType().getEnumConstants();
            String tipos;
            if (invalidFormat.getTargetType().isEnum() && constants != null) {
                tipos = java.util.Arrays.stream(constants)
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
            } else {
                tipos = invalidFormat.getTargetType().getSimpleName();
            }

            String campo = invalidFormat.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .collect(Collectors.joining("."));
            String valor = String.valueOf(invalidFormat.getValue());
            detalhe = "Campo '" + campo + "' recebeu valor inválido: " + valor + ". Tipo esperado: " + tipos;
        } else {
            ex.getMostSpecificCause();
            detalhe = ex.getMostSpecificCause().getMessage();
        }

        Map<String, String> details = Map.of("erro", detalhe);
        log.warn("JSON inválido: {}", detalhe);

        return buildErrorResponse(msg, HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDTO> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String msg = "Método HTTP não permitido: " + ex.getMethod();

        String metodosPermitidos = "";
        if (ex.getSupportedHttpMethods() != null) {
            metodosPermitidos = ex.getSupportedHttpMethods().stream()
                    .map(HttpMethod::name)
                    .collect(Collectors.joining(", "));
        }

        Map<String, String> details = Map.of("métodosPermitidos", metodosPermitidos);
        return buildErrorResponse(msg, HttpStatus.METHOD_NOT_ALLOWED, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGenericException(Exception ex) {
        log.error("Erro inesperado:", ex);
        String msg = "Ocorreu um erro inesperado. Tente novamente mais tarde.";
        Map<String, String> details = Map.of("exception", ex.getClass().getSimpleName());
        return buildErrorResponse(msg, HttpStatus.INTERNAL_SERVER_ERROR, details);
    }

    private ResponseEntity<ErrorDTO> buildErrorResponse(String message, HttpStatus status, Map<String, String> details) {
        ErrorDTO dto = new ErrorDTO(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                details
        );
        return new ResponseEntity<>(dto, status);
    }

    private ResponseEntity<ErrorDTO> buildErrorResponse(Exception ex, HttpStatus status, Map<String, String> details) {
        return buildErrorResponse(ex.getMessage(), status, details);
    }
}
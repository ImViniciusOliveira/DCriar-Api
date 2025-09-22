package com.dcriar.exception.handler;

import com.dcriar.api.dto.response.ErrorDTO;
import com.dcriar.exception.custom.*;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // ============================
    // Exceções de domínio
    // ============================

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

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErrorDTO> handleEstoqueInsuficiente(EstoqueInsuficienteException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, Map.of("produtoId", ex.getProdutoId().toString()));
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

    @ExceptionHandler(EstoqueNegativoNoCanalException.class)
    public ResponseEntity<ErrorDTO> handleEstoqueNegativoNoCanalException(EstoqueNegativoNoCanalException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, Map.of("produtoId", ex.getProdutoId().toString()));
    }

    // ============================
    // Exceções de validação
    // ============================

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

    // ============================
    // Exceções de requisições inválidas
    // ============================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> handleMalformedJson(HttpMessageNotReadableException ex) {
        String msg = "JSON malformado ou sintaxe inválida na requisição.";
        log.warn("JSON inválido: {}", ex.getMessage());
        return buildErrorResponse(msg, HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDTO> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String msg = "Método HTTP não permitido: " + ex.getMethod();
        return buildErrorResponse(msg, HttpStatus.METHOD_NOT_ALLOWED, null);
    }

    // ============================
    // Exceção genérica (catch-all)
    // ============================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGenericException(Exception ex) {
        log.error("Erro inesperado:", ex);
        String msg = "Ocorreu um erro inesperado. Tente novamente mais tarde.";
        return buildErrorResponse(msg, HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    // ============================
    // Métodos auxiliares
    // ============================

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
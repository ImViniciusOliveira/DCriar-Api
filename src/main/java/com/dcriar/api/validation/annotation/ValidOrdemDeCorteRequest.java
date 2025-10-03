package com.dcriar.api.validation.annotation;

import com.dcriar.api.dto.request.production.OrdemDeCorteRequestDTO;
import com.dcriar.api.validation.validator.OrdemDeCorteRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Anotação de validação para garantir que uma {@link OrdemDeCorteRequestDTO} seja válida.
 * <p>
 * Esta anotação é aplicada no nível da classe e utiliza o {@link OrdemDeCorteRequestValidator}
 * para implementar a lógica de validação condicional baseada no {@code modoCalculo}:
 * <ul>
 *     <li>Se o modo for 'AUTOMATICO', o campo {@code margens} é obrigatório.</li>
 *     <li>Se o modo for 'MANUAL', o campo {@code tamanhoFinal} é obrigatório.</li>
 * </ul>
 *
 * @see OrdemDeCorteRequestValidator
 * @see OrdemDeCorteRequestDTO
 */
@Documented
@Constraint(validatedBy = OrdemDeCorteRequestValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface ValidOrdemDeCorteRequest {

    String message() default "Ordem de corte inválida: verifique os campos obrigatórios para o modo de cálculo selecionado.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

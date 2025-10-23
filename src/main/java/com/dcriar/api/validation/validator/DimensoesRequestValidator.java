package com.dcriar.api.validation.validator;

import com.dcriar.api.dto.request.product.DimensoesRequestDTO;
import com.dcriar.api.validation.annotation.ValidDimensoesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Validador para o DTO {@link DimensoesRequestDTO}, acionado pela anotação {@link ValidDimensoesRequest}.
 * <p>
 * Este validador verifica se, caso o objeto não seja nulo, seus campos de dimensão são válidos:
 * <ul>
 *     <li>O {@code larguraCm} não pode ser nulo e deve ser um valor positivo.</li>
 *     <li>O {@code comprimentoCm} não pode ser nulo e deve ser um valor positivo.</li>
 * </ul>
 */
public class DimensoesRequestValidator extends BaseValidator<ValidDimensoesRequest, DimensoesRequestDTO> {

    private static final Logger log = LoggerFactory.getLogger(DimensoesRequestValidator.class);

    @Override
    protected void validate(DimensoesRequestDTO dto) {
        log.info("Validando DimensoesRequestDTO: {}", dto);
        addViolationIf(dto.getLarguraCm() == null || dto.getLarguraCm().compareTo(BigDecimal.ZERO) <= 0, "A largura deve ser um número positivo.", "larguraCm");
        addViolationIf(dto.getComprimentoCm() == null || dto.getComprimentoCm().compareTo(BigDecimal.ZERO) <= 0, "O comprimento deve ser um número positivo.", "comprimentoCm");
    }
}

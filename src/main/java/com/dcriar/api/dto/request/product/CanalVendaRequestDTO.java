package com.dcriar.api.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para requisições de criação/atualização de CanalVenda.
 * <p>
 * Centraliza validações e requisitos de negócio para entrada de dados.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanalVendaRequestDTO {
    /**
     * O nome único do canal de venda (ex: "SHOPEE", "MERCADO_LIVRE", "LOJA_FISICA").
     */
    @NotBlank(message = "O nome do canal de venda é obrigatório.")
    @Size(max = 50, message = "O nome do canal de venda deve ter no máximo 50 caracteres.")
    private String nome;
}


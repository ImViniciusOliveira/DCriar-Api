package com.dcriar.exception.custom;

/**
 * Exceção específica para violações de regras de negócio no domínio de Estoque.
 * <p>
 * Esta classe estende {@link RegraNegocioException}, reutilizando sua estrutura
 * mas fornecendo um nome semântico que torna o código mais claro e permite
 * um tratamento de erro mais específico, se necessário.
 */
public class EstoqueRegraNegocioException extends RegraNegocioException {

    /**
     * Constrói a exceção com a mensagem que descreve a regra de negócio violada.
     *
     * @param message A descrição do erro de negócio relacionado ao estoque.
     */
    public EstoqueRegraNegocioException(String message) {
        super(message);
    }
}

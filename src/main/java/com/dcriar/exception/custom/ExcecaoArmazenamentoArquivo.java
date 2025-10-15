package com.dcriar.exception.custom;

/**
 * Exceção lançada quando ocorre um erro durante as operações de armazenamento de arquivos.
 * <p>
 * Isso pode incluir problemas ao criar o diretório de upload, salvar um arquivo
 * ou qualquer outra falha de I/O relacionada ao sistema de arquivos.
 */
public class ExcecaoArmazenamentoArquivo extends RuntimeException {

    /**
     * Constrói a exceção com uma mensagem de erro.
     *
     * @param message A mensagem detalhando o erro.
     */
    public ExcecaoArmazenamentoArquivo(String message) {
        super(message);
    }

    /**
     * Constrói a exceção com uma mensagem de erro e a causa original.
     *
     * @param message A mensagem detalhando o erro.
     * @param cause   A exceção original que causou este erro (ex: IOException).
     */
    public ExcecaoArmazenamentoArquivo(String message, Throwable cause) {
        super(message, cause);
    }
}

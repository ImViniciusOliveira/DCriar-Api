    git commit -m "feat: Implementa upload de imagem e ciclo de vida para produtos" -m "Implementa a funcionalidade completa de upload de imagens e gerencia seu ciclo de vida, associando-as aos produtos.

    - Adiciona um endpoint genérico e reutilizável (/api/v1/uploads) para o upload de arquivos, que salva as imagens no sistema de arquivos local.
    - Refatora o ProdutoService para salvar apenas o caminho relativo da imagem no banco de dados, garantindo portabilidade entre ambientes.
    - Implementa a exclusão de arquivos de imagem órfãos ao atualizar ou deletar um produto, mantendo o sistema de arquivos limpo.
    - Aprimora o endpoint de busca de produtos para montar dinamicamente a URL completa da imagem na resposta, desacoplando o front-end da estrutura de armazenamento.
    - Adiciona paginação e ordenação por nome ao endpoint de listagem de produtos (/api/v1/products) para garantir uma ordem consistente e melhorar a escalabilidade.
    - Corrige links HATEOAS em ApiRootController e Assemblers para compatibilidade com os novos endpoints paginados."
    
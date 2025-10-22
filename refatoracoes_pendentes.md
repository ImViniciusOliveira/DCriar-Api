# Refatorações Pendentes e Débitos Técnicos

Este documento lista pontos de melhoria na arquitetura e no código que foram identificados mas não implementados imediatamente para não quebrar o fluxo de desenvolvimento. Eles devem ser abordados em futuras iterações para manter a qualidade e a manutenibilidade do código.

## Camada de Serviço (Service Layer)

### 1. `ProdutoServiceImpl.patch` - Lógica de Tradução de Campos

- **Problema:** O método `patch` atualmente chama um método privado `translateFieldNames` para converter nomes de campos do frontend (ex: `dimensoes`) para nomes de campos do DTO de requisição (ex: `dimensoesUnitarias`).
- **Por que é um problema?** A camada de serviço (domínio) não deveria ter conhecimento sobre a estrutura ou os nomes dos campos da camada de apresentação (API/frontend). Isso viola o princípio de separação de responsabilidades.
- **Solução Proposta:** Mover essa lógica de tradução para o `ProdutoController`. O controller receberia o `Map<String, Object>`, realizaria a tradução dos nomes dos campos e, só então, chamaria o `produtoService.patch()` com os dados já no formato esperado pela camada de serviço.
- **Referência no Código:** Procure pelo comentário `TODO: (Refatoração)` no método `ProdutoServiceImpl.patch`.

---

## ApiRoot: mover paginação para `/produtos`

- **Proposta:** Remover o link templated de paginação (`{?page,size,sort}`) do `ApiRoot` e expor apenas um link simples (ou nenhum) para navegação da navbar; adicionar/assinalar o link templated no próprio endpoint `/api/v1/produtos` (por exemplo via rel `produtos-paged` ou através de links paginados na resposta de `GET /produtos`).

- **Motivação:** O `ApiRoot` será usado apenas para descoberta rápida dos itens da navbar. A informação de paginação é relevante apenas quando o cliente efetivamente consome a coleção de produtos (GET /produtos). Manter o template de paginação no endpoint de coleção evita confusão com clientes que só leem links simples do `ApiRoot` e melhora compatibilidade retroativa.

- **Passos sugeridos:**
  1. Remover/evitar rel templated de produtos no `ApiRoot` (ou adicionar um rel simples `produtos` para compatibilidade).
  2. Garantir que `ProdutoModelAssembler` e/ou `ProdutoController#getNewProductTemplate` exponham o rel templated `produtos-paged` ou que as respostas de `GET /produtos` incluam links paginados (`self`, `first`, `next`, `last`).
  3. Atualizar o frontend para usar `produtos-paged` (ou os links paginados da própria resposta) quando precisar de paginação; manter fallback para `produtos` se o cliente não suporta templates.

- **Benefício:** Evita quebrar clientes antigos que esperam links simples no `ApiRoot` e deixa a paginação onde faz sentido: no recurso de coleção.

- **Prioridade:** Médio — Recomendado quando houver tempo para alinhar front/back (pequena alteração arquitetural com baixo risco).

---

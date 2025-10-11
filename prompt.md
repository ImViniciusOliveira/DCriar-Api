# Guia de Estilo e Arquitetura para o Projeto DCriar

Este documento define as convenções e padrões a serem seguidos pela IA ao trabalhar neste projeto.

## 1. Princípios Gerais

- **Clean Code:** Todos os princípios de Clean Code devem ser aplicados. Mantenha métodos curtos, nomes de variáveis claros e siga o Princípio da Responsabilidade Única (SRP).

## 2. Documentação e Comentários

- **Javadoc:** Nunca remova Javadoc existente. Apenas adicione ou melhore a documentação para refletir as novas mudanças. Todos os métodos públicos e classes devem ter Javadoc claro e conciso.
- **Comentários de Bloco:** Para métodos complexos ou blocos de código extensos, adicione comentários `//` que expliquem a lógica em um formato passo a passo (ex: `// 1. Buscar...`, `// 2. Mesclar...`).

## 3. Linguagem e Estilo de Código (Java)

- **Lombok:** Utilize as anotações do Lombok de forma extensiva para reduzir código boilerplate.
  - Sempre prefira o uso de `@Builder` para a construção de entidades e DTOs.
  - Use `@RequiredArgsConstructor` para injeção de dependência em services e controllers.
  - Use `@Slf4j` para logging quando necessário.

## 4. Arquitetura e Estrutura do Projeto

- **Estrutura de Pastas:** Antes de criar um novo arquivo (DTO, Exception, Service, etc.), utilize as ferramentas de busca (`find_files`, `grep`) para identificar a pasta correta com base na estrutura existente. Mantenha a consistência do projeto.
- **Camadas:** Siga estritamente a arquitetura em três camadas:
  1.  **Controller:** Responsável apenas por HTTP. Deve ser "enxuto" (thin).
  2.  **Service:** Contém toda a lógica de negócio.
  3.  **Repository:** Apenas para acesso a dados.

## 5. Commits e Git

- **Padrão:** Use o padrão **Conventional Commits** (`feat:`, `fix:`, `refactor:`, `docs:`, etc.).
- **Mensagens:** Escreva mensagens de commit claras e em português.

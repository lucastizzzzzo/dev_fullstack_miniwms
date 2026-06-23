# Parte 1 — Proposta do Mini WMS

## 1. Introdução da microaplicação

Este documento apresenta a Parte 1 do projeto acadêmico para a disciplina de Desenvolvimento Full Stack / TIS, do curso de Sistemas de Informação (7º período). Propõe-se a implementação de uma microaplicação do tipo mini WMS (Warehouse Management System) com foco no controle de estoque, localização de produtos, gestão de lotes e registro de movimentações. O escopo foi delimitado para viabilizar uma implementação completa e demonstrativa em ambiente acadêmico, respeitando a modelagem relacional mínima exigida no enunciado.

## 2. Objetivo do sistema

O objetivo geral é prover uma aplicação capaz de suportar as operações básicas de um pequeno armazém: cadastro e manutenção de produtos e endereços, controle por lotes (com validade e quantidades), geração e processamento de ordens de saída, e registro de movimentações (entradas, transferências e saídas). Adicionalmente, o sistema deve assegurar integridade referencial, consistência de saldos por lote, rastreabilidade por meio de histórico de movimentações e oferecer uma usabilidade mínima para interação e validação por avaliadores.

## 3. Descrição funcional

A microaplicação organiza-se em camadas clássicas: apresentação (interface web simples ou cliente HTTP), API REST (controladores), lógica de negócio (services) e persistência (repositórios JPA/Hibernate). O comportamento funcional contempla os seguintes fluxos:

- Cadastro e manutenção de `produto` e `endereco`.
- Registro de `lote` ao receber mercadoria, com controle de `quantidade_total`, `quantidade_reservada`, data de fabricação e validade; cada lote está associado a um `endereco`.
- Criação de `ordem_saida` contendo múltiplos `item_ordem_saida`; durante a criação, realiza-se a reserva de quantidades em lotes disponíveis segundo política FIFO (ou outra política configurável).
- Confirmação da ordem de saída que efetiva as movimentações físicas: debita-se a `quantidade_total` do lote, reduz-se `quantidade_reservada` e registra-se um registro em `movimentacao` para fins de auditoria.
- Consultas e relatórios simples: saldo por produto, histórico por lote, localizações por endereço.

Os módulos principais são: autenticação/usuários, catálogo de produtos, endereçamento físico, gestão de lotes, ordens de saída e movimentações. A camada de serviço implementará regras críticas (reserva, alocação por lote, verificação de saldo e bloqueio por validade) e garantirá que operações concorrentes sejam tratadas em transações ACID.

## 4. Estrutura de dados proposta

A modelagem atende à estrutura mínima exigida no enunciado, incluindo integridade referencial e campos necessários para operações de WMS.

Tabelas e campos essenciais (resumo):

- `usuario`
  - `id` (PK, UUID), `nome`, `email` (único), `senha_hash`, `perfil` (ex.: OPERADOR, GERENTE), `ativo`, `criado_em`, `atualizado_em`.

- `produto`
  - `id` (PK), `sku` (único), `nome`, `descricao`, `unidade_medida`, `peso` (opcional), `ativo`, timestamps.

- `endereco`
  - `id` (PK), `codigo` (ex.: A-01-03), `descricao`, `tipo` (PRATELEIRA, PALLET), `capacidade`, `ativo`.

- `lote`
  - `id` (PK), `produto_id` (FK), `endereco_id` (FK), `codigo_lote`, `quantidade_total`, `quantidade_reservada`, `data_fabricacao`, `data_validade`, `status` (DISPONIVEL, RESERVADO, BLOQUEADO), timestamps.

- `ordem_saida`
  - `id` (PK), `codigo` (único), `data_criacao`, `data_confirmacao`, `cliente`, `status` (ABERTA, RESERVADA, PROCESSADA, CANCELADA), `usuario_id` (FK).

- `item_ordem_saida`
  - `id` (PK), `ordem_saida_id` (FK), `produto_id` (FK), `lote_id` (FK, opcional), `quantidade`, `unidade`, `status_item` (PENDENTE, ATENDIDO).

- `movimentacao`
  - `id` (PK), `tipo` (ENTRADA, SAIDA, TRANSFERENCIA), `produto_id` (FK), `lote_id` (FK), `origem_endereco_id` (FK, nullable), `destino_endereco_id` (FK, nullable), `quantidade`, `data_movimentacao`, `usuario_id` (FK), `observacao`.

Relacionamentos e integridade:
- `produto` 1:N `lote`.
- `endereco` 1:N `lote`.
- `ordem_saida` 1:N `item_ordem_saida`.
- `lote` 1:N `item_ordem_saida` (quando aplicável).
- `movimentacao` referencia `produto`, `lote` e endereços de origem/destino.

Recomenda-se a adoção de exclusão lógica (`ativo`) em entidades que preservem histórico, e o uso de transações gerenciadas para operações de débito/compensação de quantidades.

## 5. Tecnologias escolhidas

- Framework web: Spring Boot (configuração mínima, integração com Spring Data e Spring Validation).
- Linguagem: Java (versão LTS recomendada).
- Banco de dados: PostgreSQL (transações e integridade referencial).
- ORM: Hibernate, via Spring Data JPA.
- Testes: JUnit 5 e MockMvc para testes de controllers e integração leve.
- Ferramenta de teste manual: Insomnia (uso restrito a testes de API; não é componente de persistência nem ORM).

Justificativa: as tecnologias são maduras, amplamente adotadas e adequadas para demonstrar arquitetura em camadas, persistência transacional e testes automatizados em um contexto acadêmico.

Dependências Spring mínimas sugeridas:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- driver `postgresql`
- `spring-boot-starter-validation`
- `spring-boot-starter-test` (escopo de teste — inclui MockMvc)

## 6. Estratégia de testes

A estratégia contempla:
- Testes de unidade: validação de regras de negócio em classes de serviço com JUnit 5 + Mockito.
- Testes de integração de camada web: uso de MockMvc para validar endpoints HTTP, códigos de status e mensagens de validação.
- Testes de persistência: `@DataJpaTest` para verificar mapeamentos; opção por Testcontainers para testes com PostgreSQL real quando necessário.

Casos críticos a cobrir: criação e edição de entidades chave, fluxo de reserva e confirmação de ordens, tentativas de operação inválida (quantidades insuficientes, lotes vencidos) e validações de entradas.

## 7. Usabilidade mínima

A interface prevê navegação simples com módulos disponíveis em menu (Produtos, Endereços, Lotes, Ordens de Saída, Movimentações, Usuários). Telas essenciais:

- Listagem com filtros e ações (editar, excluir, visualizar).
- Formulários de cadastro/edição com validação cliente/servidor e mensagens de erro claras.
- Tela de criação de ordem com seleção de itens, botão para reservar estoque e botão para confirmar processamento.
- Visualização de detalhe com histórico de movimentações por produto/lote.

Feedback ao usuário: mensagens de sucesso/erro, confirmações para ações destrutivas e indicadores de operação em andamento.

## 8. Considerações finais

A proposta apresentada respeita os requisitos mínimos do enunciado, preserva foco acadêmico e é tecnicamente viável dentro do escopo disponível. O repositório contém os fontes dos diagramas em Mermaid no diretório `diagrams/`:

- `diagrams/erd.mmd`
- `diagrams/seq_reserva.mmd`
- `diagrams/seq_confirmacao.mmd`

Como continuação, podem ser geradas a especificação OpenAPI (Swagger), esboços de controllers/DTOs em Spring Boot e a exportação dos diagramas para SVG/PNG conforme preferência do avaliador.

---

*Documento gerado automaticamente como artefato da Parte 1 do projeto Mini WMS.*

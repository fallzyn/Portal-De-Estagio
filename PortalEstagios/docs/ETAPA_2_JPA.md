# Etapa 2A — entidades JPA, repositories e consulta de áreas

**Histórico da etapa 2A:** a consulta foi confirmada no seu Codespaces. A continuação atual está em [Etapa 2B — serviços JPA](ETAPA_2B_SERVICOS_JPA.md). Nela o console e o restante do serviço deixam o JDBC manual; os testes comuns passam de 17 para 25, e a integração passa de 9 para 19 testes. Siga as instruções de substituição/remoção da etapa 2B para não misturar arquivos das versões.

A etapa 1 foi confirmada no seu Codespaces: `/api/status` respondeu `ok`. Agora vamos conferir uma consulta que realmente passa pelo JPA e pelo PostgreSQL.

Esta parte mapeia as quatro tabelas, prepara os repositories e acrescenta uma consulta pública de áreas. O `PortalService` antigo e o console continuam com JDBC durante a transição. Na **etapa 2B**, vamos migrar as demais operações e regras para repositories; só depois faremos autenticação. Não considere esta etapa uma migração completa de todos os fluxos.

## 1. Corrigir dois caminhos antes de copiar

No commit `85bab5a` que você enviou, há dois caminhos diferentes dos esperados:

- Mova `src/service/StatusController.java` para `src/controller/StatusController.java`. Ele declara `package controller;`. Não mantenha as duas cópias: isso provocaria classe duplicada.
- Mova `resource/application.properties` para `resources/application.properties`, com **s**. O `pom.xml` inclui a pasta `resources`; o arquivo no singular não estava sendo carregado. Na etapa 1, a aplicação ainda funcionava porque a porta padrão do Spring já é 8080.

Todos os caminhos desta página são relativos à pasta `PortalEstagios`.

## 2. Entender as três peças do JPA

- **JPA** é a especificação que descreve como mapear objetos Java para tabelas.
- **Hibernate** é a implementação usada pelo Spring Boot para executar esse mapeamento e gerar SQL.
- **Spring Data JPA** cria a implementação das interfaces repository, fornecendo métodos como `findById`, `save`, `findAll` e `deleteById`.

O caminho da consulta nova será:

```text
Navegador → AreaController → AreaConsultaService
          → AreaRepository → Hibernate → PostgreSQL
```

## 3. Atualizar os arquivos, nesta ordem

As mudanças preparadas aqui não são automaticamente enviadas ao GitHub. Copie ou compare os arquivos nos mesmos caminhos no seu projeto. Não substitua alterações novas suas sem comparar.

| Grupo | Arquivos | O que mudou |
| --- | --- | --- |
| Dependências | `pom.xml` | Starter Data JPA e perfil Maven `integracao` |
| Modelo | `src/model/Area.java`, `Usuario.java`, `Vaga.java`, `Candidatura.java` | Records passaram a classes `@Entity`, mapeadas às tabelas existentes |
| Repositories | `src/repository/AreaRepository.java`, `UsuarioRepository.java`, `VagaRepository.java`, `CandidaturaRepository.java` | Operações e consultas JPA |
| Descoberta | `src/app/PortalApplication.java` | `@EntityScan`, `@EnableJpaRepositories` e descoberta de serviços |
| Configuração | `resources/application.properties` | Variáveis da conexão, validação do esquema e desativação de criação automática de tabelas |
| Consulta | `src/service/AreaConsultaService.java` | Consulta de áreas dentro de uma transação de leitura |
| Resposta | `src/dto/AreaResposta.java` | Retorno contendo somente `id` e `nome` |
| Endpoint | `src/controller/AreaController.java` | `GET /api/areas` |
| Testes | `tests/StatusControllerTest.java`, `tests/JpaRepositoryIT.java` | Teste isolado do status e integração separada com PostgreSQL real |

Os enums `Perfil`, `Modalidade` e `Situacao` continuam iguais. Não apague `Main`, os DAOs JDBC nem os testes anteriores nesta etapa.

## 4. Entender o mapeamento das entidades

### `@Entity`, `@Table` e `@Id`

`@Entity` informa ao Hibernate que a classe é persistente. `@Table(name = "usuarios")`, por exemplo, aponta para a tabela que já existe. `@Id` identifica a chave primária; `@GeneratedValue(strategy = IDENTITY)` utiliza o identificador gerado pelo PostgreSQL, compatível com os `BIGSERIAL` dos scripts.

As entidades têm um construtor protegido sem argumentos para uso do JPA. Os construtores e acessores antigos, como `usuario.id()` e `vaga.empresaId()`, foram mantidos para não quebrar o console e os testes durante a migração. Um ID 0 no construtor significa objeto novo e é armazenado internamente como `null` até a inserção.

### `@Column` e enums

`@Column` preserva os nomes e comprimentos do esquema. `@Enumerated(EnumType.STRING)` grava valores como `ESTUDANTE` e `REMOTA`, e não a posição numérica do enum.

### `@ManyToOne`

Uma vaga aponta para uma empresa e uma área. Uma candidatura aponta para um estudante e uma vaga. As colunas `empresa_id`, `area_id`, `estudante_id` e `vaga_id` continuam sendo chaves estrangeiras; a diferença é que o Java passa a representar essas relações como objetos.

No fluxo JPA, crie uma vaga com a empresa e a área obtidas dos repositories. O construtor antigo que recebe somente IDs é compatibilidade com JDBC/testes, não o caminho recomendado para gravar associações novas via JPA.

As relações usam `LAZY`: o Hibernate pode carregá-las quando necessárias. Por isso, consultas que precisam dessas relações usam `@EntityGraph`, e a camada de serviço coordena a transação. Não foram adicionadas exclusões em cascata; excluir uma área não deve apagar suas vagas.

### Data de inscrição

A coluna original `data_inscricao` é `TIMESTAMP`. O atributo persistente agora é `LocalDateTime`, preservando data e hora; novas inscrições JPA usam UTC. O método antigo `dataInscricao()` continua retornando a data para o console, e `dataInscricaoCompleta()` expõe o valor completo no código Java.

## 5. Entender a configuração antes de iniciar

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.sql.init.mode=never
spring.datasource.hikari.maximum-pool-size=5
```

- As três variáveis passam a ser necessárias para **iniciar o servidor web**, não apenas para o console.
- `validate` confere a correspondência entre entidades e tabelas. Não cria, atualiza nem recria o esquema.
- `spring.sql.init.mode=never` impede executar os scripts SQL automaticamente no início.
- `open-in-view=false` deixa o carregamento de relações sob responsabilidade das transações da aplicação.
- O pool mantém no máximo cinco conexões nesta configuração inicial.

Não troque `validate` por `create`, `create-drop` ou `update` para contornar um erro no Supabase. Se o esquema não corresponder, investigaremos a diferença e criaremos uma migração incremental quando necessária.

## 6. Testar primeiro sem banco

Na pasta `PortalEstagios`:

```bash
mvn clean test
```

O esperado continua sendo **17 testes, 0 falhas e 0 erros**. Nesta etapa, o teste de status usa `MockMvc`, que testa a rota sem subir toda a aplicação ou conectar ao PostgreSQL. A consulta HTTP real e a persistência são verificadas separadamente na integração.

## 7. Configurar a conexão no Codespaces

No mesmo terminal Bash que iniciará o servidor:

```bash
export DB_URL='jdbc:postgresql://SEU_HOST:5432/postgres?sslmode=require'
export DB_USER='postgres.SEU_PROJETO'
read -rsp 'Senha do banco Supabase: ' DB_PASSWORD
echo
export DB_PASSWORD

mvn spring-boot:run
```

Copie host e usuário de Supabase → Connect → Session pooler → JDBC. Substitua os exemplos; não coloque a senha no código, no Git ou na conversa. Para PostgreSQL local, use o endereço, banco e usuário da instalação local.

O usuário do banco precisa conseguir ler os metadados e tabelas do esquema existente. Não rode novamente `01_criacao.sql` ou `02_dados.sql` no Supabase do grupo: eles já foram executados.

No Codespaces, abra a URL encaminhada da porta 8080 e acrescente:

```text
/api/areas
```

Com os dados de exemplo originais, a resposta será parecida com:

```json
[
  {"id":2,"nome":"Administração"},
  {"id":3,"nome":"Design"},
  {"id":1,"nome":"Tecnologia"}
]
```

Os IDs ou a quantidade podem variar se vocês já alteraram as áreas. A ordenação é por nome. Essa rota faz somente leitura. Não há login ou endpoints de gravação nesta parte da migração.

Se o servidor falhar antes de iniciar, confira primeiro as três variáveis, a pasta `resources` e a mensagem inicial do erro. Não envie a senha. `Schema-validation` indica diferença entre o modelo e o banco, e não um pedido para recriar as tabelas.

## 8. Como foram verificados os repositories

A implementação foi validada com PostgreSQL 17 **local e separado**, aplicando os scripts originais ao banco `portal_estagios_test`. Não foi usado H2, nem seu Supabase.

O comando de integração é:

```bash
mvn clean verify -Pintegracao
```

Ele precisa de um PostgreSQL local exclusivo, com `01_criacao.sql` e `02_dados.sql` já aplicados. Antes do comando, configure:

```bash
export TEST_DB_URL='jdbc:postgresql://127.0.0.1:5432/portal_estagios_test'
export TEST_DB_USER='SEU_USUARIO_LOCAL'
read -rsp 'Senha do banco local de testes: ' TEST_DB_PASSWORD
echo
export TEST_DB_PASSWORD
```

O teste recusa hosts remotos e bancos com outro nome para evitar execução acidental no Supabase. Não reutilize credenciais de produção. Este teste não é necessário para o primeiro checkpoint no seu Codespaces, caso você só tenha o Supabase disponível.

Resultado validado nesta implementação: **17 testes da suíte comum + 9 testes de integração, todos aprovados**. A integração cobre:

1. Validação e leitura do esquema/dados originais sem recriar tabelas.
2. Filtros de vagas abertas por área e modalidade.
3. Edição e exclusão de área livre.
4. Rejeição de e-mail duplicado no PostgreSQL.
5. Gravação de candidatura com timestamp e filtro por estudante.
6. Rejeição de candidatura duplicada no PostgreSQL.
7. Rejeição de exclusão de área utilizada por vaga.
8. Encerramento preservando candidaturas e retirando a vaga da consulta de abertas.
9. Resposta real de `GET /api/areas`, passando por controller, serviço, repository e banco.

Cada teste usa transação com rollback para seus registros temporários. As sequências de IDs do PostgreSQL podem avançar mesmo com rollback, o que é normal. Mensagens de restrição SQL nos testes que verificam duplicidades/exclusões são esperadas; o resultado a conferir é `BUILD SUCCESS`, sem falhas no relatório.

## Checkpoint para a próxima parte

Avançaremos quando `mvn clean test` passar no seu projeto e `/api/areas` mostrar os dados reais pelo JPA. Depois migraremos `PortalService` e as operações de gravação para repositories, antes de desenvolver login, senhas e permissões da API.

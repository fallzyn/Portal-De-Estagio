# Etapa 2B — regras e operações do serviço com JPA

O checkpoint 2A foi confirmado no Codespaces: `/api/areas` retornou as áreas do banco. Agora o restante do serviço e o console passam a utilizar JPA, preservando as regras da Entrega 1. A autenticação será a próxima etapa.

## O que você precisa entender

Antes, o menu criava `PortalService` manualmente com os DAOs JDBC. Esses DAOs abriam conexões e escreviam SQL. Agora o Spring cria os repositories, injeta-os no `PortalService` e administra as transações e conexões.

```text
Console Main ou AreaController
          ↓
     PortalService (regras e transação)
          ↓
     Repositories Spring Data JPA
          ↓
     Hibernate → PostgreSQL/Supabase
```

JPA não elimina o driver JDBC: o Hibernate ainda o utiliza para conversar com PostgreSQL. O que saiu foi o SQL manual dos antigos DAOs.

## 1. Aplicar a atualização no seu projeto

Use esta atualização somente depois dos arquivos da etapa 2A. As mudanças preparadas nesta conversa não são automaticamente enviadas para seu GitHub. Compare arquivos que tenham alterações próprias antes de substituir.

Todos os caminhos abaixo são relativos à pasta `PortalEstagios`.

| Ação | Arquivos |
| --- | --- |
| Substituir o serviço | `src/service/PortalService.java` |
| Substituir o console | `src/app/Main.java` |
| Atualizar as entidades | `src/model/Vaga.java`, `src/model/Candidatura.java` |
| Atualizar consultas | `src/repository/AreaRepository.java`, `UsuarioRepository.java`, `VagaRepository.java` |
| Atualizar a rota de áreas | `src/controller/AreaController.java` |
| Substituir os testes de regras | `tests/RegrasTest.java` |
| Atualizar a integração dos repositories | `tests/JpaRepositoryIT.java` |
| Adicionar os testes do serviço | `tests/PortalServiceIT.java` |
| Adicionar a configuração comum dos testes PostgreSQL | `tests/BancoPostgresTestSupport.java` |

Depois de copiar esses arquivos, remova **apenas os fontes antigos**:

- A pasta `src/dao`, com suas cinco interfaces DAO, cinco implementações JDBC e `Conexao.java`.
- O arquivo `src/service/AreaConsultaService.java`: sua consulta foi incorporada a `PortalService.areas()`.

Não apague `src/repository`, `sql`, as tabelas ou o banco. Remover um arquivo Java não remove os dados do Supabase. Os arquivos do banco e as configurações da etapa 2A continuam iguais.

**A remoção dos fontes antigos é necessária:** os novos testes e o console usam repositories, e os construtores de compatibilidade exclusivos do JDBC foram retirados das entidades. Não misture versões das duas etapas. O pacote desta etapa inclui `REMOVER_ETAPA_2B.txt` com a lista exata de arquivos obsoletos; ele não executa exclusões automaticamente.

## 2. Entender a injeção pelo construtor

`PortalService` agora recebe quatro dependências:

```java
public PortalService(UsuarioRepository usuarios, AreaRepository areas,
                     VagaRepository vagas, CandidaturaRepository candidaturas)
```

`@Service` faz o Spring descobrir essa classe. A aplicação já descobre `service` e `repository` pela configuração da etapa 2A. Não use `new PortalService(...)` no controller ou console. Em testes unitários, o construtor continua sendo usado para fornecer repositories simulados.

`AreaController` também recebe `PortalService` pelo construtor. Assim a consulta de áreas passou pelo mesmo serviço das demais operações.

## 3. Entender as transações

`@Transactional` na classe delimita uma unidade de trabalho para cada operação de negócio. Se uma exceção de execução escapar, as gravações dessa transação são desfeitas. Métodos de consulta têm `@Transactional(readOnly = true)`.

`saveAndFlush` solicita a gravação e envia o SQL ao banco ainda dentro da transação. **Flush não é commit:** a transação ainda pode ser desfeita se outra operação falhar. Um teste de integração verifica essa diferença criando um cadastro e provocando uma falha posterior na mesma transação; o cadastro não permanece gravado.

As entidades existentes são carregadas dos repositories antes de serem relacionadas ou editadas. A edição da vaga muda os campos permitidos no objeto existente e preserva o proprietário e a situação.

## 4. Regras preservadas

- Cadastro público aceita estudante ou empresa; não cria administrador.
- E-mail repetido e área com nome repetido são rejeitados.
- Só empresa publica; edição e encerramento exigem ser a proprietária.
- Título, descrição, localização, modalidade, área e carga horária são validados.
- Só estudante se candidata; vaga encerrada ou inscrição repetida são rejeitadas.
- Encerrar a vaga preserva candidaturas existentes e a retira da consulta pública.
- Consultas de inscrições filtram pelo estudante; candidatos exigem a empresa proprietária.
- Só administrador gerencia áreas e consulta contagens.
- Área usada por uma vaga não pode ser excluída, e a FK do PostgreSQL permanece protegendo o vínculo.

As restrições `UNIQUE` e as chaves estrangeiras do banco continuam sendo a proteção final, inclusive quando duas operações chegam próximas no tempo.

### Bloqueio da vaga

`VagaRepository.bloquearPorId` usa `PESSIMISTIC_WRITE` dentro da transação. Candidatar, editar e encerrar passam por esse método antes de decidir/gravar. Isso impede verificar uma situação antiga e gravar depois de um encerramento concorrente já concluído. O bloqueio pertence à transação e é liberado ao final dela; não é um bloqueio permanente da vaga.

Consultas públicas continuam sem esse bloqueio. Não se trata de autenticação: a identidade real virá do Spring Security na próxima etapa. Os IDs de perfil ainda são usados somente pelo console simulado e pelos testes, não por endpoints web de gravação.

## 5. Conferir os testes sem banco

Na pasta `PortalEstagios`:

```bash
mvn clean test
```

O resultado esperado mudou para **25 testes, zero falhas, zero erros e zero ignorados**:

- 19 em `RegrasTest`, com repositories simulados por Mockito.
- 5 em `MainTest`, verificando o console sem operações de banco.
- 1 em `StatusControllerTest`, verificando a rota de status isoladamente.

Os 11 cenários de regras anteriores foram preservados e ampliados. Não exclua um teste que falhar: confira se todos os arquivos desta etapa foram copiados e se os fontes obsoletos foram removidos.

## 6. Testar o console JPA no Codespaces

Pare o servidor web com `Ctrl+C` para usar o mesmo terminal que já contém suas variáveis de conexão. Caso abra outro terminal, configure `DB_URL`, `DB_USER` e `DB_PASSWORD` novamente, conforme a etapa 2A.

Execute:

```bash
mvn compile exec:java
```

O menu continua disponível. O Spring/JPA inicializa quando uma operação precisa do banco. O contexto do console é iniciado com `WebApplicationType.NONE`, sem ocupar a porta HTTP 8080. Ao sair do menu, esse contexto é fechado.

Faça este checkpoint:

1. Escolha **5**, para consultar áreas pelo serviço JPA.
2. Escolha **4**, deixe os filtros vazios e confira as vagas abertas.
3. Escolha **3** e selecione o administrador da lista, conferindo seu ID.
4. Escolha **6 → 5**, para ver as contagens administrativas.
5. Escolha **0**, para sair normalmente.

Os números das contagens dependem dos dados reais do seu banco. Compare-os no Supabase; não espere necessariamente os mesmos valores do banco local usado nos testes.

Se quiser confirmar uma gravação, cadastre uma única vez um estudante fictício com um e-mail ainda não utilizado, por exemplo `teste.jpa.2b@exemplo.com`, e confira no SQL Editor:

```sql
SELECT id, nome, email, perfil, curso
FROM usuarios
WHERE email = 'teste.jpa.2b@exemplo.com';
```

Esse cadastro será salvo no seu Supabase. Nesta etapa ele ainda não tem senha de login; isso será tratado na migração incremental da etapa 3. Não execute novamente os scripts de criação das tabelas.

## 7. Conferir que a rota web continua funcionando

Saia do console e execute:

```bash
mvn spring-boot:run
```

Abra a URL encaminhada da porta 8080 com `/api/areas`. A resposta continua mostrando as áreas, agora passando pelo `PortalService` migrado. Não abrimos endpoints web de gravação nesta etapa: eles receberão a proteção e os DTOs nas próximas partes.

Se a porta estiver ocupada, pare a instância antiga ou use outra porta conforme a etapa 1. A aplicação não fica pronta somente porque os testes sem banco passaram: confira também as consultas com suas variáveis de conexão.

## 8. Verificação realizada nesta implementação

Executado em PostgreSQL 17 local separado, sem usar H2 e sem acessar o Supabase do grupo:

```bash
mvn clean verify -Pintegracao
```

O perfil usa as variáveis `TEST_DB_URL`, `TEST_DB_USER` e `TEST_DB_PASSWORD`, conforme o guia 2A. A configuração comum foi movida para `BancoPostgresTestSupport`, que continua recusando hosts remotos e bancos diferentes de `portal_estagios_test`.

Resultado: **25 testes sem banco + 19 de integração, todos aprovados**, divididos em:

- 9 testes dos repositories e da rota real de áreas, já existentes na etapa 2A.
- 10 testes de `PortalServiceIT`, cobrindo cadastro, duplicidade, publicação, filtros, inscrições, encerramento, propriedade, separação de consultas, áreas, contagens, perfis e rollback.

Os registros temporários dos testes de integração são desfeitos por rollback. As sequências de IDs podem avançar, o que é normal. O console também foi exercitado contra esse banco separado para verificar que o comando Maven inicializa JPA, grava e consulta de verdade, e fecha o contexto ao sair.

## Próximo checkpoint

Depois de confirmar os 25 testes e o console JPA no seu Codespaces, seguimos para a **etapa 3: migração incremental de senha/hash e autenticação com Spring Security**. Não publique a versão como Entrega 2 final antes de concluir autenticação, endpoints protegidos, interface e respectivas verificações.

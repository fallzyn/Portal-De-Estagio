# Migração para a Entrega 2, uma etapa por vez

Objetivo: evoluir o portal de console para Spring Boot, JPA, Spring Security e uma interface web simples, conforme o professor solicitou. Cada etapa terá uma verificação antes da seguinte. Este documento diferencia o que já foi implementado do que ainda é planejamento.

**Etapa atual 2B:** a etapa 2A foi confirmada no Codespaces. As demais operações do serviço e o console foram migrados para repositories JPA. O guia atual é [Etapa 2B — serviços JPA](ETAPA_2B_SERVICOS_JPA.md), com os arquivos a substituir/remover e os 25 testes comuns + 19 de integração. As seções abaixo registram a preparação e os checkpoints anteriores. Autenticação continua sendo a próxima etapa.

**Atualização da etapa 2A:** a etapa 1 foi confirmada no Codespaces. Foram preparados os mapeamentos JPA, repositories e uma consulta de áreas; consulte [Etapa 2 — JPA](ETAPA_2_JPA.md) para os arquivos e comandos atuais. Com JPA, iniciar o servidor agora exige as variáveis de conexão. As instruções de servidor sem banco abaixo registram o estado anterior da etapa 1. A migração das demais operações do serviço ainda será feita na etapa 2B.

## Sequência de trabalho

| Etapa | O que vamos construir | Verificação para avançar |
| --- | --- | --- |
| 1 — Spring Boot | Servidor HTTP e rota `GET /api/status` | Testes antigos preservados e resposta JSON real pela rede local |
| 2 — JPA e PostgreSQL | Entidades, relacionamentos e repositories para usuários, áreas, vagas e candidaturas | Consultas e gravações JPA em PostgreSQL de teste; compatibilidade com o esquema existente |
| 3 — Migração e autenticação | Migração SQL incremental para hash de senha, Spring Security, cadastro, login/logout e administrador controlado | Senha armazenada como hash, senha errada rejeitada e logout invalidando a sessão |
| 4 — Endpoints e autorização | DTOs, validação, erros HTTP e operações de cada perfil | Proprietário obtido da sessão, acesso a dados de terceiros bloqueado e nenhuma senha/hash nas respostas |
| 5 — Interface | Páginas simples dos fluxos obrigatórios | Fluxos de estudante e empresa pelo navegador, com token CSRF nas operações autenticadas |
| 6 — Entrega | Verificação integrada, documentação da API, diagramas, registros de IA e ZIP | Cenários do enunciado executados com evidências reais e instruções reproduzíveis |

As etapas 1 e 2A estão preparadas. Não há autenticação ou endpoints web de gravação nesta parte; a nova persistência JPA já atende à consulta pública de áreas.

## Etapa 1 — Entender os arquivos

### 1. `pom.xml`: preparar as dependências

O projeto usa o parent `spring-boot-starter-parent` na versão 3.5.16, publicada no Maven Central e compatível com Java 17. O parent mantém versões compatíveis das bibliotecas.

- `spring-boot-starter-web`: servidor Tomcat embutido, recebimento de requisições HTTP e conversão das respostas para JSON.
- `spring-boot-starter-test`: JUnit e ferramentas de teste do Spring. Substitui a dependência isolada de JUnit; os testes antigos continuam funcionando.
- Driver PostgreSQL: mantido para o console existente e para a migração futura. O driver sozinho não conecta o servidor ao banco.
- `spring-boot-maven-plugin`: permite `mvn spring-boot:run` e gera um JAR executável.
- `exec-maven-plugin`: mantido para executar o console antigo com `mvn compile exec:java`.

Os diretórios `src` e `tests` continuam iguais aos da Entrega 1. O novo diretório `resources` é incluído no classpath pelo Maven. Não é necessário reorganizar todos os arquivos agora.

### 2. `src/app/PortalApplication.java`: iniciar a aplicação web

Essa é a nova classe de entrada do servidor. `SpringApplication.run(...)` cria o contexto Spring e inicia o Tomcat.

`@SpringBootApplication` habilita a configuração automática. Como o projeto tem pacotes irmãos (`app`, `controller`, `service`, etc.), nesta etapa indicamos explicitamente os pacotes `app` e `controller` para a busca de componentes. Acrescentaremos os componentes necessários quando migrarmos as próximas camadas.

A classe `Main` continua iniciando o console. São dois comandos distintos; o servidor web não deve abrir o menu interativo.

### 3. `src/controller/StatusController.java`: responder uma requisição

- `@RestController`: a resposta do método será enviada no corpo HTTP, como JSON.
- `@RequestMapping("/api")`: define o prefixo das URLs deste controller.
- `@GetMapping("/status")`: recebe uma consulta GET em `/api/status`.
- O `Map` retornado contém apenas o nome da aplicação e seu estado de resposta. Nenhum DAO, senha ou variável de conexão é acessado.

O caminho desta primeira requisição é:

```text
Navegador → Tomcat → StatusController → resposta JSON
```

Nas etapas seguintes, os controllers de negócio chamarão os serviços, que usarão os repositories JPA para acessar PostgreSQL.

### 4. `resources/application.properties`: nome e porta

```properties
spring.application.name=portal-estagios
server.port=${PORT:8080}
```

A porta padrão é 8080. Se existir a variável de ambiente `PORT`, ela será utilizada. Não há credenciais nesse arquivo.

### 5. `tests/StatusControllerTest.java`: provar que o servidor responde

O teste inicia o Spring Boot em uma porta aleatória e usa o cliente HTTP do Java para fazer uma chamada real. Confere código 200, tipo `application/json` e os dois valores esperados no corpo.

Ele complementa os 11 testes de regras e os 5 testes do console: o total esperado desta etapa é **17 testes**, sem necessidade de configurar banco. Um processo iniciado ou uma porta aberta, sozinhos, não seriam uma verificação suficiente.

## Etapa 1 — Executar no VS Code

As alterações preparadas neste ambiente ainda não são automaticamente enviadas ao GitHub ou ao seu computador. Confira os arquivos modificados/adicionados e leve-os para o mesmo caminho no seu checkout antes de usar os comandos abaixo. Não substitua alterações suas sem comparar.

### A. Abrir o terminal na pasta correta

Abra `PortalEstagios`, onde está `pom.xml`. Confirme:

```bash
java -version
mvn -version
```

Use um JDK 17 ou superior (não apenas JRE) e Maven 3.9 ou superior. No `mvn -version`, confira também qual Java o Maven utiliza.

### B. Executar os testes

```bash
mvn clean test
```

Resultado esperado: 17 testes, 0 falhas, 0 erros e 0 ignorados, seguido de `BUILD SUCCESS`.

### C. Iniciar o servidor

```bash
mvn spring-boot:run
```

O terminal fica ocupado enquanto o servidor está funcionando. Para este comando da etapa 1, não é necessário configurar `DB_URL`, `DB_USER` nem `DB_PASSWORD`.

### D. Fazer a consulta

No navegador do seu computador, abra:

```text
http://localhost:8080/api/status
```

Resposta esperada (a ordem dos campos não importa):

```json
{"aplicacao":"portal-estagios","status":"ok"}
```

No Codespaces, use a URL encaminhada da porta 8080 na aba Ports e acrescente `/api/status`. Abrir somente `/` pode retornar 404: ainda não criamos a página inicial.

Se a porta 8080 estiver ocupada, inicie em outra porta:

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Nesse caso, consulte `http://localhost:8081/api/status`. Use `Ctrl+C` para parar.

### E. Empacotamento opcional

```bash
mvn clean package
java -jar target/portal-estagios-1.0.jar
```

Esse JAR inicia `PortalApplication`, e não o console. A pasta `target` contém resultados de compilação; não deve fazer parte do ZIP entregue. O `.gitignore` novo ignora arquivos futuros dessa pasta, mas não retira automaticamente os binários que já estavam versionados na Entrega 1.

## Próxima etapa — JPA, sem apagar o banco

Depois de confirmar a etapa 1 no seu computador, vamos:

1. Acrescentar `spring-boot-starter-data-jpa` e configurar a conexão pelas variáveis de ambiente.
2. Mapear as tabelas existentes com entidades `@Entity`, identificadores `@Id` e relacionamentos `@ManyToOne`.
3. Criar repositories Spring Data JPA e migrar a persistência usada pelos serviços.
4. Validar o esquema e testar as operações em um PostgreSQL separado de teste antes de usar o Supabase do grupo.

Não executaremos novamente os scripts de criação sobre o Supabase já preenchido. Não usaremos `ddl-auto=create` ou `create-drop` nele. A alteração para armazenar hashes será uma migração incremental posterior, preservando usuários, vagas e candidaturas. Contas antigas sem hash precisarão de um procedimento controlado para definir senha; não vamos inventar uma senha compartilhada.

O login será responsabilidade do Spring Security da aplicação. O Supabase continuará fornecendo PostgreSQL; Supabase Auth não substituirá automaticamente o requisito do professor.

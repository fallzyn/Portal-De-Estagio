# Portal de Estágios — Entrega 2

Aplicação Java 17 com Spring Boot, Spring Security, Spring Data JPA e PostgreSQL. Oferece consulta pública de vagas, cadastro e login por sessão, operações de empresa e estudante e API administrativa. A migração para JPA segue a orientação do professor relatada pelo grupo.

## Integrantes

- Henrique Haramaki Mantaveli — 10752924
- Lukas Barone Sussa — 10753291
- Ryan Silva de Sousa — 10757255

## Executar no Codespaces ou Linux/Bash

Pré-requisitos: JDK 17 ou superior, Maven 3.9+, PostgreSQL acessível. Na raiz do repositório, entre em `PortalEstagios` (no Codespaces: `/workspaces/Portal-De-Estagio/PortalEstagios`).

```bash
cd PortalEstagios
export DB_URL='jdbc:postgresql://SEU_HOST:5432/postgres?sslmode=require'
export DB_USER='postgres.SEU_PROJETO'
read -rsp 'Senha do banco: ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
mvn clean test
mvn spring-boot:run
```

No Supabase, use host e usuário do Session pooler, tipo JDBC. Para PostgreSQL local, use, por exemplo, `jdbc:postgresql://localhost:5432/portal_estagios` e o usuário da sua instalação. A senha do banco é diferente da senha de login do portal. Novos terminais podem exigir exportar novamente as variáveis. Não grave credenciais em código, README ou Git.

Abra `http://localhost:8080/portal.html`. No Codespaces, abra a porta 8080 em **Portas** e acrescente `/portal.html` à URL encaminhada. Login está em `/login`, cadastro em `/cadastro.html` e conta/saída em `/conta.html`. A rota `/` leva à conta autenticada; a consulta pública está em `/portal.html`. Pare o servidor com Ctrl+C.

### PowerShell

```powershell
cd PortalEstagios
$env:DB_URL = 'jdbc:postgresql://SEU_HOST:5432/postgres?sslmode=require'
$env:DB_USER = 'postgres.SEU_PROJETO'
$senhaBanco = Read-Host 'Senha do banco' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $senhaBanco).Password
Remove-Variable senhaBanco
mvn clean test
mvn spring-boot:run
```

## Preparar o banco

**Banco já usado nas etapas anteriores:** preserve os dados. Se `usuarios.senha_hash` já existe, esta versão não exige novo SQL. Não execute os scripts iniciais novamente sobre o Supabase preenchido.

**Banco novo:** crie um banco local ou projeto Supabase e execute, na ordem, `sql/01_criacao.sql`, `sql/02_dados.sql`, `sql/03_hash_senha.sql`. Os scripts contêm dados fictícios. Exemplo para um PostgreSQL local com acesso pelo seu usuário:

```bash
createdb portal_estagios
psql -d portal_estagios -f sql/01_criacao.sql
psql -d portal_estagios -f sql/02_dados.sql
psql -d portal_estagios -f sql/03_hash_senha.sql
```

A aplicação usa `ddl-auto=validate` e não recria tabelas. O banco deve estar configurado e acessível antes de iniciar. Usuários fictícios antigos sem hash não possuem senha padrão e não conseguem entrar. Crie estudantes/empresas pelo formulário para testar com senhas escolhidas por você.

## Administrador de demonstração — inicialização controlada

O cadastro público rejeita administradores. Para preparar uma conta administrativa, pare o servidor e, no terminal já configurado com DB_URL/DB_USER/DB_PASSWORD, execute uma vez:

```bash
export PORTAL_ADMIN_INICIALIZAR=true
export PORTAL_ADMIN_EMAIL='admin@exemplo.com'
read -rsp 'Escolha a senha do administrador: ' PORTAL_ADMIN_PASSWORD
printf '\n'
export PORTAL_ADMIN_PASSWORD
mvn spring-boot:run
```

Escolha pelo menos 8 caracteres, até 72 bytes UTF-8. A inicialização cria o administrador ou define o hash de um administrador existente sem senha. Não promove estudante/empresa e não altera senha já definida. Não existe senha administrativa padrão. O professor pode usar banco próprio e definir sua própria senha por esse procedimento.

Após conferir o login, pare com Ctrl+C e desative a inicialização:

```bash
unset PORTAL_ADMIN_INICIALIZAR PORTAL_ADMIN_EMAIL PORTAL_ADMIN_PASSWORD
mvn spring-boot:run
```

No PowerShell, use `$env:PORTAL_ADMIN_INICIALIZAR='true'`, `$env:PORTAL_ADMIN_EMAIL='admin@exemplo.com'` e leia a senha com Read-Host -AsSecureString como no exemplo do banco; atribua o valor convertido a `$env:PORTAL_ADMIN_PASSWORD`. Depois de parar a primeira execução, remova as três variáveis com `Remove-Item Env:PORTAL_ADMIN_INICIALIZAR, Env:PORTAL_ADMIN_EMAIL, Env:PORTAL_ADMIN_PASSWORD`.

## Funcionalidades e interface

- Público: vagas abertas, filtros por área/modalidade e cadastro de estudante/empresa.
- Estudante: candidatura, rejeição de duplicidade/vaga encerrada e consulta somente das próprias inscrições.
- Empresa: publicação, listagem própria, edição e encerramento; candidatos somente de suas vagas.
- Administrador: contagens e cadastro, edição e exclusão de áreas pela API. Área em uso não pode ser excluída.
- Todos os perfis: login, logout, sessão e senha com BCrypt; identidade obtida da autenticação.

A tela `/portal.html` adapta as ações à conta conectada. O backend valida perfil e propriedade mesmo quando a URL é acessada diretamente. Fechar uma vaga preserva candidaturas; reabertura não faz parte do escopo. A administração pode ser demonstrada por cliente de API, conforme permitido no enunciado.

[Documentação completa da API, autenticação e exemplos](docs/API.md). [Instalação da versão completa e roteiro manual](docs/ENTREGA_2_GUIA.md). [Resultados de verificação](docs/RESULTADOS_ENTREGA_2.md).

## Arquitetura e migração

Requisição HTTP → Spring Security → controller → serviço → repository JPA → PostgreSQL. DTOs controlam entradas e respostas. `PortalService` preserva as regras de negócio; `PortalWebService` monta respostas dentro da transação para não acessar relacionamentos LAZY depois que a sessão JPA foi fechada. As exceções de negócio preservam a hierarquia IllegalArgumentException usada no console, com subclasses para 403, 404 e 409 na API.

A Entrega 1 usava console → serviço → interfaces DAO → JDBC. Foram reaproveitados o domínio, os enums, as regras, as tabelas, dados e testes de negócio. DAO/JDBC foi substituído por repositories JPA e transações Spring; foram acrescentados hash, autenticação real, DTOs, controllers e HTML/JavaScript. A lambda de filtragem em PortalService e a interface funcional Acao do console continuam explicáveis e úteis.

[Modelo atualizado e esquema das tabelas](docs/modelo.md). [Diagrama de arquitetura e fluxo de candidatura](docs/ARQUITETURA_ENTREGA_2.md). O console legado permanece com `mvn compile exec:java`, mas sua seleção simulada de identidade não representa a autenticação da Entrega 2. Para a avaliação, use a aplicação web.

## Testes

```bash
mvn clean test
```

São 58 testes sem banco na versão completa. Para os testes de integração, prepare um PostgreSQL **local exclusivo**, chamado `portal_estagios_test`, com os três scripts SQL e os dados iniciais uma única vez. Não use o banco de demonstração já alterado nem o Supabase:

```bash
export TEST_DB_URL='jdbc:postgresql://localhost:5432/portal_estagios_test'
export TEST_DB_USER='SEU_USUARIO_LOCAL'
read -rsp 'Senha do PostgreSQL local de testes: ' TEST_DB_PASSWORD
printf '\n'
export TEST_DB_PASSWORD
mvn clean verify -Pintegracao
```

O perfil acrescenta 33 testes de integração. O suporte rejeita URLs remotas e nome de banco diferente. Os cenários usam rollback ou removem apenas seus próprios registros fictícios. O banco inicial deve conter somente os dados dos scripts, sem contas adicionadas pela demonstração, pois alguns testes conferem contagens exatas. Não é necessário executar integração para iniciar o portal.

As regras de candidatura duplicada, vaga encerrada e propriedade de outra empresa estão automatizadas em testes de serviço e HTTP. Os detalhes e limites da verificação estão no relatório. A execução feita pelo assistente foi em PostgreSQL local separado; não comprova configuração do Supabase do avaliador.

## Documentação e entrega

- [Enunciado fornecido pelo grupo](docs/ENUNCIADO_PROJETO.md).
- [Checklist da Entrega 2](docs/CHECKLIST_ENTREGA_2.md).
- [Três registros de uso de IA para conferência do grupo](docs/REGISTRO_IA_ENTREGA_2.md).
- Guias `ETAPA_*` registram a evolução incremental; o presente README, API.md e ENTREGA_2_GUIA.md descrevem a versão integrada.

A entrega solicitada é link do repositório e ZIP da versão entrega-2, sem target, credenciais ou pastas de compilação. O assistente não realizou commit, push ou envio ao Moodle. Cada integrante deve revisar o código e conseguir explicar e alterar as operações sem consultar IA, conforme o enunciado.

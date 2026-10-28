# Portal de Estágios — Entrega 1

Projeto em Java 17 com menu no console, PostgreSQL, JDBC e DAO, desenvolvido para a primeira entrega da disciplina.

## Integrantes

- Henrique Haramaki Mantaveli - 10752924
- Lukas Barone Sussa - 10753291
- Ryan Silva de Sousa - 10757255


## Funcionalidades

- Cadastro de estudantes e empresas, com e-mail único.
- Seleção de identidade simulada (sem senha nesta entrega).
- Consulta de vagas abertas com filtros por área e modalidade.
- Empresas: publicação, listagem, edição e encerramento de suas vagas; consulta dos candidatos.
- Estudantes: candidatura e consulta das próprias inscrições.
- Administrador: cadastro, listagem, edição e exclusão de áreas; contagens administrativas.
- Persistência em PostgreSQL por JDBC, com instruções SQL parametrizadas.

## Requisitos

- JDK 17 ou superior.
- Maven 3.9 ou superior.
- PostgreSQL em execução.

## Preparação do banco

1. Criar o banco `portal_estagios` no PostgreSQL.
2. Executar `sql/01_criacao.sql` e, depois, `sql/02_dados.sql` nesse banco.
3. Configurar as variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD` no terminal ou no sistema.

Exemplo de URL: `jdbc:postgresql://localhost:5432/portal_estagios`.

No PowerShell, exemplo (substituir os valores locais; não enviar credenciais ao repositório):

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/portal_estagios"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "SUA_SENHA_LOCAL"
```

## Como executar

No diretório `PortalEstagios`:

```bash
mvn compile exec:java
```

Para executar os testes automatizados:

```bash
mvn test
```

Os testes de `tests/RegrasTest.java` utilizam DAOs fictícios e **não dependem de um banco de dados**. Já a execução do menu depende do PostgreSQL configurado.

## Organização das camadas

- `src/app`: entrada e saída do console e escolha de ações por perfil.
- `src/model`: entidades e enums (`Usuario`, `Area`, `Vaga`, `Candidatura`, etc.).
- `src/service`: regras de negócio, validações, perfis e propriedade dos registros.
- `src/dao`: interfaces e implementações JDBC que acessam o PostgreSQL.
- `sql`: criação das tabelas e dados fictícios.
- `docs/modelo.md`: modelo de domínio e associações.
- `tests`: testes automatizados de regras de negócio.

### Exemplo: caminho de uma candidatura

Menu (`Main`) → `PortalService.candidatar` → interface `CandidaturaDAO` → `JdbcCandidaturaDAO` → PostgreSQL.

O serviço verifica se o usuário é estudante e se a vaga está aberta. A restrição `UNIQUE(estudante_id, vaga_id)` evita duplicidade no banco.

### Expressão lambda / interface funcional

O menu usa a interface funcional `RunnableSQL` para executar ações e centralizar o tratamento de erros. A consulta de vagas também utiliza um `Predicate<Vaga>` para filtrar vagas abertas.

## Modelo de domínio

Consultar `docs/modelo.md`. O esquema relacional está em `sql/01_criacao.sql`.

## Verificação manual (preencher após executar)

| Cenário | Resultado esperado | Resultado observado | Situação |
| --- | --- | --- | --- |
| Cadastrar estudante/empresa com e-mail válido | Cadastro realizado | [preencher] | [pendente] |
| Repetir e-mail | Cadastro rejeitado | [preencher] | [pendente] |
| Publicar vaga válida | Vaga criada | [preencher] | [pendente] |
| Publicar com título vazio ou carga horária 0 | Operação rejeitada | [preencher] | [pendente] |
| Filtrar vagas abertas por área e modalidade | Apenas vagas correspondentes | [preencher] | [pendente] |
| Candidatura válida | Candidatura registrada | [preencher] | [pendente] |
| Repetir candidatura | Operação rejeitada | [preencher] | [pendente] |
| Candidatar-se a vaga encerrada | Operação rejeitada | [preencher] | [pendente] |
| Outra empresa editar/encerrar vaga ou consultar candidatos | Acesso negado | [preencher] | [pendente] |
| Outro estudante consultar candidaturas | Apenas candidaturas próprias | [preencher] | [pendente] |
| Excluir área utilizada por vaga | Exclusão rejeitada | [preencher] | [pendente] |
| Reiniciar o sistema | Dados continuam no banco | [preencher] | [pendente] |
| Administrador consultar contagens | Quantidades apresentadas | [preencher] | [pendente] |

### Testes automatizados

`RegrasTest.java` contempla os três cenários exigidos: candidatura duplicada, vaga encerrada e tentativa de acessar vaga de outra empresa. Registrar o resultado real de `mvn test`: **[pendente de execução pelo grupo]**.

## Registro de uso de IA

O professor exige **três interações relevantes por entrega**, contendo problema, ferramenta e prompt, proposta da IA, decisão do grupo, mudanças realizadas e verificação. Completar com fatos e verificações reais, sem inventar evidências.

### Interação 1 — Revisão do modelo de domínio

- **Problema:** identificar possíveis erros no código e conferir os requisitos da Entrega 1.
- **Ferramenta e prompt:**  ChatGPT - "Pode checar pra mim os códigos, se está errado ou se falta algo em que o projeto citou e você não colocou no arquivo zip?"
- **Proposta da IA:** revisar os arquivos Java, verificar os requisitos e identificar possíveis problemas, incluindo a validação de e-mail.
- **Decisão do grupo:** registrar após vocês analisarem e aceitarem as correções, porque...
- **Mudanças realizadas:** correção da expressão regular de validação de e-mail.
- **Verificação:** conferência da compilação com Java 21; a execução completa com PostgreSQL ainda está pendente.

### Interação 2 — Regras de negócio e JDBC/DAO

- **Problema:** código com pouca formatação, dificultando a leitura e a manutenção.
- **Ferramenta e prompt:** ChatGPT - "Pra finalizar, deixe o código mais legível, eles estão tudo em poucas linhas e estão bem extensas."
- **Proposta da IA:** reorganizar os arquivos Java, separando instruções e aplicando indentação.
- **Decisão do grupo:** preencher depois que vocês conferirem a nova formatação.
- **Mudanças realizadas:** reorganização e formatação dos 22 arquivos Java.
- **Verificação:** comparação dos arquivos originais com os revisados e compilação das classes principais.

### Interação 3 — Revisão de código e documentação

- **Problema:** documentação incompleta e necessidade de conferir os itens exigidos pelo professor.
- **Ferramenta e prompt:** ChatGPT — Me ajude a fazer p README; Além disso, pode checar pra mim os códigos, se está errado ou se falta algo em que o projeto citou e você não colocou no arquivo zip.
- **Proposta da IA:** Corrigir a expressão regular do e-mail, reorganizar a formatação dos arquivos e completar o README com instruções e roteiro de testes.
- **Decisão do grupo:** preencher depois de revisar a documentação, porque...
- **Mudanças realizadas:** atualização do README e criação de um checklist de entrega.
- **Verificação:** comparação da documentação com o enunciado; os resultados dos testes práticos ainda precisam ser registrados.

## Limitações e próxima etapa

Nesta entrega, a identidade é simulada por seleção de usuário. A autenticação, o Spring Security, a API REST e a interface web serão desenvolvidos na Entrega 2. Não armazenar senhas reais no repositório.

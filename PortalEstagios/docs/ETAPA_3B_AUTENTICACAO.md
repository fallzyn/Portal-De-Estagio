# Etapa 3B — cadastro, login e sessão

Esta etapa liga o hash BCrypt da etapa 3A ao Spring Security. Mantém as entidades, os repositories e as regras de PortalService. Acrescenta cadastro com senha para estudante/empresa e login/logout com sessão. Os endpoints de vagas e candidaturas serão a próxima etapa.

## 1. Copiar os arquivos

Extraia o pacote na raiz do repositório, preservando a pasta PortalEstagios. Substitua os arquivos existentes e acrescente os novos. Não deixe cópias das classes em outros pacotes. O projeto continua usando `src`, `tests` e `resources` diretamente; não crie uma segunda estrutura `src/main`.

A base necessária é a etapa 3A funcionando, com a coluna `usuarios.senha_hash`. Se ela já foi criada no Supabase, **não execute novamente os scripts de criação nem os dados iniciais**. Esta etapa não exige novo SQL.

## 2. Verificar a compilação

No Bash do Codespaces:

```bash
cd /workspaces/Portal-De-Estagio/PortalEstagios
mvn clean test
```

Esses testes usam simulações para o banco. O perfil `integracao` acrescenta testes com PostgreSQL local exclusivo, chamado `portal_estagios_test`; ele rejeita endereços do Supabase. Não configure suas credenciais reais nas variáveis de testes.

## 3. Iniciar com seu banco

Pare a execução anterior com Ctrl+C. No mesmo terminal, configure a conexão, substituindo HOST e PROJETO pelos dados do Session pooler:

```bash
export DB_URL='jdbc:postgresql://SEU_HOST:5432/postgres?sslmode=require'
export DB_USER='postgres.SEU_PROJETO'
read -rsp 'Senha do banco Supabase: ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
mvn spring-boot:run
```

Se essas variáveis já estiverem definidas no terminal, basta executar o último comando. `DB_PASSWORD` é a senha do banco; não é a senha de login de um usuário.

## 4. Conferir no navegador

Abra a URL encaminhada da porta 8080 do Codespaces, acrescentando os caminhos abaixo. Localmente, use `http://localhost:8080`.

1. `/login`: tela de login do portal, processada pelo Spring Security. O campo **E-mail** recebe o e-mail completo da conta. Em **Senha**, digite a senha usada para gerar seu hash, não o próprio hash. Se o programa GerarHash pediu a senha novamente dentro do Java, vale a senha dessa segunda entrada.
2. Entre com a conta cujo hash você já salvou. Deve abrir `/conta.html`, mostrando seu nome, e-mail, perfil e as áreas.
3. Clique em **Sair**. Abrir `/conta.html` novamente deve levar ao login.
4. Tente uma senha errada: deve aparecer uma mensagem genérica, sem informar se o e-mail existe.
5. Abra `/cadastro.html` e crie um estudante fictício com um e-mail ainda não usado. Informe curso e senha de pelo menos 8 caracteres, até 72 bytes UTF-8 (acentos podem ocupar mais de um byte). Faça login com essa conta.
6. Repita com uma empresa: deve pedir nome da empresa, sem exigir curso. O cadastro grava o hash automaticamente; não precisa executar SQL.
7. Conectado como estudante, abra `/api/admin/contagens`: esperado 403. Somente um administrador autenticado pode consultar essas contagens. O cadastro público não cria administradores.
8. Após sair, `/api/auth/eu` deve retornar 401. A consulta pública `/api/areas` continua disponível.

Cadastros feitos nesse servidor ficam no banco indicado em DB_URL. Portanto, se você configurar o Supabase, os novos usuários de teste ficarão lá. Os testes realizados pelo assistente usam outro PostgreSQL local.

Contas antigas com `senha_hash` nulo continuam no banco, mas não entram. Defina a senha de cada conta escolhida pelo procedimento da etapa 3A; não existe senha padrão. A senha do banco nunca deve ser usada como senha de uma conta.

## O que cada parte faz

- `SecurityConfig`: configura autenticação, sessão, permissões e CSRF. Mantém os três perfis existentes.
- `PortalUserDetailsService`: busca o usuário pelo e-mail no repository.
- `UsuarioAutenticado`: representa a identidade autenticada; mantém ID e perfil na sessão e descarta a credencial após a autenticação.
- `CadastroEntrada`: recebe apenas os campos permitidos; rejeita campos adicionais. A senha não é serializada em respostas.
- `CadastroService`: valida o perfil, gera BCrypt e salva o usuário com hash dentro da mesma transação, reutilizando PortalService.
- `AuthController`: cadastro, identificação da conta e obtenção do token CSRF. O próprio Spring Security processa login e logout.
- `AdminController`: consulta contagens usando o ID da sessão. O cliente não escolhe a identidade do administrador.
- `ApiErros`: devolve erros sem expor SQL ou valores de senha.
- `resources/static`: telas simples de cadastro e conta, com JavaScript para chamar a API. Não são ainda a interface completa de vagas/candidaturas.

## Contrato HTTP

| Método e caminho | Acesso e resultado |
| --- | --- |
| GET /api/status, /api/areas | Público |
| GET /api/auth/csrf | Público; retorna token, headerName e parameterName |
| POST /api/auth/cadastro | Público com CSRF; JSON; 201 com ID, nome, e-mail e perfil; duplicidade 409 |
| POST /api/auth/login | Formulário com username=e-mail e password=senha; exige CSRF |
| GET /api/auth/eu | Autenticado; retorna identidade segura; 401 sem sessão |
| GET /api/admin/contagens | Administrador; 403 para outros perfis |
| POST /logout | Exige token CSRF atual; invalida sessão e redireciona ao login |

Para clientes de API, envie `Accept: application/json` no login: sucesso 200, credenciais inválidas 401. Envie os campos como `application/x-www-form-urlencoded`, não JSON. Preserve o cookie de sessão entre as chamadas. Antes de um POST, obtenha `/api/auth/csrf` e envie o token no cabeçalho indicado. Após login, obtenha um novo token, pois o anterior é invalidado. As telas fazem isso automaticamente.

A sessão usa cookie JSESSIONID HttpOnly e SameSite=Lax; expira após 30 minutos de inatividade. Não há JWT nem armazenamento de senha no navegador. Reiniciar o servidor encerra as sessões em memória, mas preserva os dados no banco. O logout real usa POST; visitar GET /logout pode mostrar a confirmação padrão do framework. O caminho /login redireciona para /login.html.

## Limites e próxima etapa

O console legado continua disponível para diagnóstico e demonstração das regras JPA; sua seleção de usuário é simulada. O método antigo de cadastro do console não define senha. Para testar autenticação e criar contas com senha, use as telas web desta etapa.

As regras de prefixos de empresa/estudante já estão configuradas, mas os endpoints correspondentes ainda serão implementados. A próxima etapa é expor as operações de vagas e candidaturas, sempre obtendo a identidade da sessão e preservando as verificações de propriedade do serviço. Esta etapa não conclui a entrega inteira.

## Resultado da verificação desta etapa

- 43 testes sem banco: aprovados, sem falhas, erros ou ignorados.
- 26 testes de integração em PostgreSQL local isolado: aprovados, sem falhas, erros ou ignorados.
- Navegador Chromium: cadastro de estudante e empresa, senha incorreta, login, áreas, bloqueio administrativo e logout aprovados, sem erros JavaScript.
- A suíte de integração confirmou que o hash salvo corresponde à senha e que nem senha nem hash aparecem nas respostas.
- O assistente não testou esta etapa contra o Supabase do grupo e não realizou commit/push.

A conferência real encontrou e corrigiu dois problemas: redirecionamento de páginas protegidas sem sessão e a tela após senha incorreta. O portal agora possui formulário próprio de login em português, processado pelo Spring Security. O checkpoint do grupo é repetir os passos da seção 4 no Codespaces.

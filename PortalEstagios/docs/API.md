# API REST — Entrega 2

Base local: http://localhost:8080. No navegador do Codespaces, use o domínio encaminhado da porta 8080. O corpo JSON usa os nomes e enums abaixo. Campos adicionais são rejeitados; proprietário e perfil de uma operação são obtidos da sessão.

## Autenticação e CSRF

Login cria sessão em memória com cookie JSESSIONID HttpOnly, SameSite=Lax; 30 minutos sem atividade expiram a sessão. Reiniciar o servidor exige novo login, mas não apaga dados. O cookie não deve ser copiado para código nem armazenado manualmente no JavaScript. O navegador o envia com `credentials: 'same-origin'`.

Antes de POST, PUT, PATCH ou DELETE, obtenha GET /api/auth/csrf com o mesmo cookie. Envie `token` no cabeçalho chamado `headerName`, ou no campo `parameterName` do formulário. Obtenha um token novo após login. O JavaScript das telas faz isso, inclusive no cadastro, login e logout. A aplicação é de mesma origem; não precisa liberar CORS.

| Método e rota | Perfil / entrada | Sucesso |
| --- | --- | --- |
| GET /api/status | Público | 200, identificação e status |
| GET /api/areas | Público | 200, lista de {id,nome} |
| GET /api/vagas?area=1&modalidade=REMOTA | Público, filtros opcionais | 200, lista de vagas abertas |
| GET /api/auth/csrf | Público | 200, {token,headerName,parameterName} |
| POST /api/auth/cadastro | Público + CSRF, JSON Cadastro | 201, identidade sem credenciais |
| POST /api/auth/login | Público + CSRF, formulário username/password | 200 JSON se Accept: application/json, ou redirecionamento para conta |
| GET /api/auth/eu | Autenticado | 200, {id,nome,email,perfil} |
| POST /logout | Autenticado + CSRF | 302, /login.html?logout; sessão invalidada |
| GET /api/empresa/vagas | EMPRESA | 200, vagas da própria empresa, inclusive encerradas |
| POST /api/empresa/vagas | EMPRESA + CSRF, JSON Vaga | 201, vaga publicada |
| PUT /api/empresa/vagas/{id} | EMPRESA proprietária + CSRF, JSON Vaga completo | 204 |
| PATCH /api/empresa/vagas/{id}/encerramento | EMPRESA proprietária + CSRF, sem corpo | 204 |
| GET /api/empresa/vagas/{id}/candidatos | EMPRESA proprietária | 200, lista de {id,nome,email,curso} |
| POST /api/estudante/candidaturas | ESTUDANTE + CSRF, {"vagaId":1} | 201, candidatura com dados da vaga |
| GET /api/estudante/candidaturas | ESTUDANTE | 200, somente inscrições da sessão |
| GET /api/admin/contagens | ADMINISTRADOR | 200, estudantes,empresas,vagasAbertas,vagasEncerradas,candidaturas |
| POST /api/admin/areas | ADMINISTRADOR + CSRF, {"nome":"Saúde"} | 201, {id,nome} |
| PUT /api/admin/areas/{id} | ADMINISTRADOR + CSRF, {"nome":"Saúde e bem-estar"} | 204 |
| DELETE /api/admin/areas/{id} | ADMINISTRADOR + CSRF | 204; 409 se vinculada a vaga |

A listagem pública e as listagens privadas são arrays JSON; ausência de resultados é `[]` com 200. Não há reabertura de vagas. Não existe rota para escolher o estudante cujas candidaturas consultar. Parâmetros como `?estudante=999` não mudam a identidade da sessão.

## Corpos de requisição e resposta

Cadastro de estudante (POST /api/auth/cadastro):

```json
{"nome":"Aluno de demonstração","email":"aluno.demo@exemplo.com","perfil":"ESTUDANTE","curso":"Sistemas de Informação","senha":"ESCOLHA_UMA_SENHA_EXCLUSIVA"}
```

Empresa usa `perfil: "EMPRESA"` e `nomeEmpresa` no lugar de `curso`. Administrador é rejeitado. Senha exige no mínimo 8 caracteres e no máximo 72 bytes UTF-8. E-mail é único e a busca de login diferencia maiúsculas/minúsculas. Use o mesmo e-mail e a senha original do cadastro. O hash nunca é devolvido.

Vaga (POST ou PUT):

```json
{"titulo":"Estágio Java","descricao":"Apoio ao desenvolvimento","areaId":1,"localizacao":"São Paulo","modalidade":"HIBRIDA","cargaHoraria":30}
```

Não envie `id`, `empresaId`, `perfil` ou `situacao` no corpo. Título, descrição, localização e área são obrigatórios; cargaHoraria é inteiro positivo; modalidades: PRESENCIAL, REMOTA, HIBRIDA. Confira os IDs existentes em GET /api/areas. A resposta 201 acrescenta id, empresaId, nomeEmpresa, nomeArea e situacao ABERTA. PUT envia os mesmos campos da criação e mantém empresa e situação.

Candidatura (POST):

```json
{"vagaId":1}
```

Resposta 201 (exemplo fictício):

```json
{"id":5,"dataInscricao":"2026-11-17T02:00:00","vaga":{"id":1,"empresaId":3,"nomeEmpresa":"Tech Exemplo","titulo":"Estágio Java","descricao":"Apoio ao desenvolvimento","areaId":1,"nomeArea":"Tecnologia","localizacao":"São Paulo","modalidade":"HIBRIDA","cargaHoraria":30,"situacao":"ABERTA"}}
```

A data é armazenada em UTC. O encerramento não remove inscrições e a mesma candidatura passa a mostrar a situação ENCERRADA.

## Exemplos com curl no Bash

Execute no terminal da máquina onde a aplicação roda. Usa Python 3 apenas para ler o JSON dos tokens. `portal.cookies` e o token ficam em /tmp, fora do repositório. Não envie esses arquivos ao chat ou ao Git.

```bash
umask 077
PORTAL_BASE='http://localhost:8080'
PORTAL_COOKIES=$(mktemp /tmp/portal-cookies.XXXXXX)
portal_token() {
  PORTAL_TOKEN=$(curl -fsS -b "$PORTAL_COOKIES" -c "$PORTAL_COOKIES" "$PORTAL_BASE/api/auth/csrf" | python3 -c 'import sys,json; print(json.load(sys.stdin)["token"])')
}
read -rp 'E-mail da conta: ' PORTAL_LOGIN_EMAIL
read -rsp 'Senha da conta: ' PORTAL_LOGIN_SENHA
printf '\n'
portal_token
printf '%s' "$PORTAL_LOGIN_SENHA" | curl -sS -b "$PORTAL_COOKIES" -c "$PORTAL_COOKIES" \
  -H 'Accept: application/json' -H "X-CSRF-TOKEN: $PORTAL_TOKEN" \
  --data-urlencode "username=$PORTAL_LOGIN_EMAIL" --data-urlencode 'password@-' \
  "$PORTAL_BASE/api/auth/login"
unset PORTAL_LOGIN_SENHA
portal_token
curl -sS -b "$PORTAL_COOKIES" "$PORTAL_BASE/api/auth/eu"
```

Autenticado como empresa, exemplo de publicação:

```bash
portal_token
curl -i -b "$PORTAL_COOKIES" -c "$PORTAL_COOKIES" \
  -H "X-CSRF-TOKEN: $PORTAL_TOKEN" -H 'Content-Type: application/json' \
  -d '{"titulo":"Estágio Java","descricao":"Apoio ao desenvolvimento","areaId":1,"localizacao":"São Paulo","modalidade":"HIBRIDA","cargaHoraria":30}' \
  "$PORTAL_BASE/api/empresa/vagas"
```

Autenticado como administrador, exemplo de área (use uma conta com esse perfil; faça novo login se necessário):

```bash
portal_token
curl -i -b "$PORTAL_COOKIES" -H "X-CSRF-TOKEN: $PORTAL_TOKEN" \
  -H 'Content-Type: application/json' -d '{"nome":"Área demonstrativa"}' \
  "$PORTAL_BASE/api/admin/areas"
curl -i -b "$PORTAL_COOKIES" "$PORTAL_BASE/api/admin/contagens"
# Substitua ID_REAL pelo ID retornado; excluir área usada deve retornar 409.
portal_token
curl -i -X DELETE -b "$PORTAL_COOKIES" -H "X-CSRF-TOKEN: $PORTAL_TOKEN" \
  "$PORTAL_BASE/api/admin/areas/ID_REAL"
portal_token
curl -i -X POST -b "$PORTAL_COOKIES" -H "X-CSRF-TOKEN: $PORTAL_TOKEN" "$PORTAL_BASE/logout"
rm -f "$PORTAL_COOKIES"
unset PORTAL_TOKEN PORTAL_COOKIES
```

## Erros

Erros das operações têm `mensagem` e `detalhes`, sem senha/hash/SQL/stack trace:

```json
{"mensagem":"A vaga pertence a outra empresa.","detalhes":[]}
```

- 400: JSON/campos/enum/validação inválidos, inclusive campos extras de proprietário ou perfil.
- 401: sessão ausente ou credenciais inválidas no login JSON.
- 403: perfil incompatível, propriedade de outra empresa ou CSRF ausente/inválido. Verificação CSRF pode responder 403 antes da verificação de login.
- 404: área ou vaga inexistente.
- 409: e-mail/área/candidatura duplicados, vaga encerrada ou área em uso.
- 503: indisponibilidade de acesso à persistência durante a operação. Falha de conexão na inicialização impede iniciar o servidor.

As telas de login usam redirecionamento e mensagem genérica; chamadas de API usam Accept: application/json. Não presuma que uma página de erro do navegador, sozinha, identifica a regra que falhou.

# Etapa 4A — consultar vagas abertas pela API

Pré-requisito: etapa 3B funcionando, com login e saída já conferidos. Esta etapa acrescenta somente a leitura pública de vagas; publicação, edição e candidaturas serão os próximos passos. Não altera tabelas, dados iniciais ou dependências do Maven.

## 1. Copiar os arquivos

Extraia o pacote na raiz do repositório, preservando a pasta PortalEstagios. Confira cada arquivo:

| Arquivo relativo a PortalEstagios | Ação |
| --- | --- |
| src/controller/VagaController.java | Criar |
| src/dto/VagaResposta.java | Criar |
| src/config/SecurityConfig.java | Substituir pela versão do pacote |
| src/controller/ApiErros.java | Substituir pela versão do pacote |
| tests/VagaControllerTest.java | Criar |
| tests/VagasIT.java | Criar |
| docs/ETAPA_4A_CONSULTA_VAGAS.md | Criar |
| README.md | Atualizar com a versão do pacote, preservando registros próprios do grupo |

Não precisa trocar pom.xml, PortalApplication.java, PortalService.java ou as entidades. PortalApplication já deve incluir `security` em scanBasePackages, conforme a correção da etapa anterior.

## 2. Verificar os testes antes de iniciar

No Bash do Codespaces:

```bash
cd /workspaces/Portal-De-Estagio/PortalEstagios
mvn clean test
```

Na base completa das etapas anteriores, o esperado é **54 testes aprovados**: os 43 anteriores e 11 novos. Caso seu projeto ainda tenha 40 anteriores por faltar UsuarioAutenticadoTest.java, a contagem será 51; copie também aquele teste da etapa 3B para completar a base.

Esses testes não exigem conexão nem gravam no Supabase. O teste VagasIT roda somente com o perfil de integração e PostgreSQL local exclusivo, com o nome portal_estagios_test. O suporte de testes rejeita URLs remotas, incluindo Supabase. Não execute os testes de integração contra o banco do grupo.

## 3. Iniciar o servidor

Se o servidor estiver executando, pare com Ctrl+C. No mesmo terminal em que as variáveis DB_URL, DB_USER e DB_PASSWORD estão configuradas, execute:

```bash
mvn spring-boot:run
```

Se abrir um terminal novo e as variáveis não estiverem disponíveis, configure novamente a conexão pelo procedimento da etapa 3B. A senha do banco continua fora dos arquivos e do chat.

## 4. Conferir uma consulta por vez no navegador

Use a mesma URL da porta 8080 do Codespaces, trocando apenas o caminho após o domínio. Localmente, a base é http://localhost:8080.

| Caminho | Resultado esperado |
| --- | --- |
| /api/vagas | HTTP 200; lista somente vagas abertas |
| /api/areas | Consulte os IDs reais das áreas do seu banco |
| /api/vagas?area=1 | HTTP 200; vagas abertas da área 1, se houver |
| /api/vagas?modalidade=REMOTA | HTTP 200; somente vagas abertas remotas |
| /api/vagas?area=1&modalidade=HIBRIDA | HTTP 200; aplica os dois filtros juntos |
| /api/vagas?modalidade=PRESENCIAL | HTTP 200; somente vagas abertas presenciais |
| /api/vagas?modalidade=ONLINE | HTTP 400; mensagem explicando os filtros permitidos |
| /api/vagas?area=abc | HTTP 400; ID deve ser numérico |
| /api/vagas?area=0 | HTTP 400; ID deve ser positivo |

O ID 1 é apenas um exemplo. Confira os IDs com /api/areas. Use PRESENCIAL, REMOTA ou HIBRIDA em maiúsculas e sem acento. Omita o parâmetro quando não quiser filtrar por ele.

Uma combinação sem vagas retorna `[]` com HTTP 200. Isso é um resultado válido, não uma falha. Uma área positiva inexistente também retorna uma lista vazia. Nenhuma dessas consultas cria ou altera registros. É possível testá-las sem login; as rotas administrativas continuam protegidas.

A resposta de cada vaga contém:

```json
{
  "id": 1,
  "empresaId": 3,
  "nomeEmpresa": "Empresa Exemplo",
  "titulo": "Estágio em Desenvolvimento",
  "descricao": "Apoio à equipe de desenvolvimento",
  "areaId": 1,
  "nomeArea": "Tecnologia",
  "localizacao": "São Paulo",
  "modalidade": "HIBRIDA",
  "cargaHoraria": 30,
  "situacao": "ABERTA"
}
```

Os valores acima são ilustrativos. O servidor retorna uma lista desses objetos, conforme os registros existentes no banco configurado em DB_URL. Não expõe o e-mail, a senha, o hash ou o nome pessoal do responsável da empresa.

## Como o código funciona

1. VagaController recebe o GET e converte os parâmetros para Long e Modalidade. Rejeita IDs não positivos antes de consultar o serviço.
2. PortalService.abertas é o mesmo método já usado pelo console; continua controlando a regra de consultar vagas abertas.
3. VagaRepository.listarAbertas aplica os filtros e ordena pelo ID. O EntityGraph existente carrega empresa e área junto da consulta, permitindo montar a resposta depois do encerramento da transação sem carregar relações tardiamente.
4. VagaResposta é um record DTO: escolhe os dados que vão para o JSON. Não devolvemos a entidade Usuario inteira.
5. SecurityConfig libera somente GET /api/vagas. Não acrescenta permissão de gravação e mantém CSRF e as regras por perfil.
6. ApiErros transforma modalidade desconhecida ou ID não numérico em HTTP 400 com mensagem legível, sem mostrar detalhes internos.

A alteração de produção se resume a dois arquivos novos e dois arquivos ajustados. Os métodos, variáveis e classes de domínio existentes foram preservados.

## Checkpoint antes de avançar

Envie o resultado de mvn clean test e confira primeiro /api/vagas. Depois teste um filtro que corresponda às vagas existentes e uma combinação sem resultados. O cadastro, login e logout devem continuar funcionando. A próxima etapa será publicar vagas autenticado como empresa, usando o ID da sessão e as regras de PortalService.

## Verificação realizada pelo assistente

- `mvn clean verify -Pintegracao`: BUILD SUCCESS.
- 54 testes sem banco: zero falhas, erros ou ignorados.
- 27 testes de integração com PostgreSQL local exclusivo: zero falhas, erros ou ignorados.
- VagasIT usa servidor HTTP real, sessão anônima e dados fictícios isolados. Confere ambos os filtros, ausência de resultados, exclusão de vagas encerradas, respostas sem dados privados e acesso administrativo ainda protegido. Remove somente os registros que cria.
- A conexão usada foi PostgreSQL local, não o Supabase do grupo. A verificação no Codespaces com seu banco é o checkpoint da seção anterior.
- Nenhum commit ou push foi realizado pelo assistente.

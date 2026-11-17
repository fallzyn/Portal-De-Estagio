# Resultados da verificação — Entrega 2 integrada

## Ambiente e limites

Verificação realizada pelo assistente no workspace desta conversa: JDK 21 compilando para Java 17, Maven 3.9.9, Spring Boot 3.5.16, PostgreSQL 17 local exclusivo em 127.0.0.1:15432/portal_estagios_test e navegador Chromium headless via Playwright. Não foi usado H2. Não foi usado nem alterado o Supabase do grupo. As contas e vagas de navegador eram fictícias e foram removidas somente desse banco local ao terminar.

A execução ocorreu em cópia temporária para não alterar arquivos target historicamente versionados. O pacote final exclui target e credenciais. Nenhum commit, push ou envio ao Moodle foi realizado pelo assistente.

## Suíte automatizada final

Comando: `mvn verify -Pintegracao` com TEST_DB_URL/TEST_DB_USER/TEST_DB_PASSWORD apontando exclusivamente para o PostgreSQL local de testes. Resultado: **BUILD SUCCESS; 91 testes no total, 0 falhas, 0 erros, 0 ignorados.**

| Suíte | Testes | Resultado |
| --- | ---: | --- |
| VagaControllerTest | 11 | Passou |
| UsuarioAutenticadoTest | 3 | Passou |
| StatusControllerTest | 1 | Passou |
| SenhasTest | 4 | Passou |
| RegrasTest | 19 | Passou |
| MainTest | 5 | Passou |
| InicializacaoAdminTest | 4 | Passou |
| AutenticacaoWebTest | 11 | Passou |
| **Subtotal de mvn test** | **58** | **Passou** |
| VagasIT | 1 | Passou |
| PortalServiceIT | 10 | Passou |
| JpaRepositoryIT | 9 | Passou |
| FluxosRestIT | 6 | Passou |
| AutenticacaoIT | 7 | Passou |
| **Subtotal de integração** | **33** | **Passou** |

Um método de integração pode conferir várias requisições relacionadas; os números acima contam métodos/invocações de teste JUnit, não cada assert ou cada clique. Os logs resumidos estão em docs/evidencias.

## Cenários obrigatórios do enunciado

| Entrada/cenário | Resultado esperado | Resultado observado | Situação |
| --- | --- | --- | --- |
| Cadastro válido de estudante/empresa | 201; hash salvo; login possível | Hash BCrypt conferido com PasswordEncoder; identidade segura e login real | Passou |
| E-mail repetido | Rejeitar sem novo usuário | 409, quantidade preservada | Passou |
| Publicação válida por empresa | 201; dona vem da sessão | Vaga persistida com empresa autenticada; listada somente pela dona | Passou |
| Título ausente/vazio ou carga 0, negativa e não numérica | 400; nenhuma vaga criada | Todas rejeitadas; listagem própria permaneceu vazia | Passou |
| Consulta pública de vagas abertas e filtros | 200; filtros combinados; sem encerradas | Apenas vagas correspondentes; combinação sem resultados retornou [] | Passou |
| Modalidade desconhecida ou área não numérica | 400 | Mensagem JSON sem exceção interna | Passou |
| Candidatura válida | 201; inscrição própria | Inscrição gravada e exibida na interface e na API | Passou |
| Repetir candidatura | 409; uma única inscrição | Duplicidade bloqueada no serviço e no HTTP | Passou |
| Candidatar em vaga encerrada | 409 | Nova candidatura rejeitada; anterior preservada | Passou |
| Empresa B editar/encerrar/ver candidatos de vaga de A | 403 em cada operação | Três operações bloqueadas; vaga permaneceu aberta e intacta | Passou |
| Estudante B consultar usando ?estudante=ID_DE_A | Não obter dados de A | Lista vazia para B; consulta usa identidade de B | Passou |
| Enviar empresaId/estudanteId no JSON | 400; não trocar dono | Campos adicionais rejeitados | Passou |
| Excluir área em uso | 409; vínculos preservados | Exclusão bloqueada | Passou |
| Área livre: criar/editar/excluir | 201/204/204 | Operações concluídas como administrador | Passou |
| Registro inexistente | 404 | Área inexistente rejeitada com código apropriado | Passou |
| Login com senha incorreta | Mensagem genérica; 401 em API | Credenciais rejeitadas | Passou |
| Logout e acesso posterior | Invalidar sessão; 401 na API | Sessão invalidada, página protegida voltou ao login | Passou |
| Sem login ou perfil incorreto | 401 ou 403, conforme autenticação/CSRF | Rotas privadas protegidas no acesso direto | Passou |
| POST sem CSRF | 403; não gravar | Login/cadastro/candidatura/publicação protegidos | Passou |
| Cadastro público de administrador | 400; sem conta criada | Perfil administrativo rejeitado | Passou |
| Respostas sem senha/hash | Não expor credenciais | DTOs de conta/vaga/candidatura/candidatos sem credenciais | Passou |
| Inicializar administrador | Somente explicitamente; sem promoção/senha padrão | Inicialização local com login real; testes confirmaram que não promove estudante nem redefine hash existente | Passou |
| Contagens administrativas | 200 para admin; 403 para outros | Contagens verificadas no serviço e acessíveis pela API administrativa | Passou |
| Reiniciar a aplicação | Vaga/inscrição persistidas; novo login | Processo encerrado e iniciado; vaga ENCERRADA e candidatura recuperadas no navegador | Passou |

## Conferência no navegador

Fluxo com duas empresas e dois estudantes distintos: consulta pública; cadastro e login; publicação; edição; candidatura; rejeição de duplicidade; listagens próprias isoladas; consulta de candidato pela dona; encerramento com confirmação; tentativa de inscrição em página antiga após encerramento; inscrição anterior exibindo ENCERRADA. Inicialização de administrador, login administrativo e criação/exclusão de área também aprovados. Não foram detectados erros JavaScript nas páginas. A nova execução após reinício recuperou a vaga e a candidatura persistidas.

Não foi realizado teste de carga nem implantação de produção; esses itens não fazem parte do escopo obrigatório. O comportamento no Codespaces/Supabase do grupo depende da cópia completa dos arquivos e das variáveis corretas. O roteiro de instalação e repetição está em ENTREGA_2_GUIA.md.

## Ajustes feitos na conclusão

- Operações novas reaproveitam PortalService, com a identidade real da sessão em vez da seleção simulada do console.
- PortalWebService monta DTOs dentro da transação para acessar relacionamentos JPA com segurança, mantendo open-in-view=false.
- As subclasses de exceção distinguem 403, 404 e 409 sem expor SQL/stack trace e continuam compatíveis com IllegalArgumentException no console e nos testes anteriores.
- Inicialização administrativa é explícita, desativada por padrão e sem credencial fixa.
- Interface mínima concluída com HTML/JavaScript e requisições de mesma origem; operações de escrita enviam CSRF.

Pode aparecer aviso de depreciação de AntPathRequestMatcher da versão atual do Spring Security. É um aviso, não uma falha de compilação ou de testes; a rota e suas permissões foram verificadas. Os avisos de instrumentação Mockito em JDK recente também não representam falhas na suíte.

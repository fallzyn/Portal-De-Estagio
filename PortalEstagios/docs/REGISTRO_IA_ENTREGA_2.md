# Registro de uso de IA — Entrega 2

Ferramenta: ChatGPT/Codex. Este registro resume fatos desta conversa e da implementação. O grupo deve conferir a redação, acrescentar suas decisões individuais e estudar o código; a IA não substitui a avaliação individual sem consulta.

## 1. Migração para Spring Boot e JPA

- Problema: evoluir o console e JDBC para aplicação web preservando regras e dados existentes.
- Prompt do grupo: “O professor disse pra gente migrar para o JPA, então preciso do passo a passo de tudo, desde do spring boot, autenticação e migração, endpoints e etc.”
- Proposta: configurar Spring Boot, mapear entidades e relacionamentos, introduzir repositories e transações, reaproveitando PortalService e as tabelas PostgreSQL.
- Decisão observada: o grupo pediu uma etapa por vez e informou ter aplicado as alterações e executado os testes; a conversa registra confirmações de 25 e depois 29 testes aprovados nas etapas correspondentes.
- Mudanças: PortalApplication, entidades JPA, repositories e migração dos serviços. JDBC/DAO deixou de ser usado pela versão integrada.
- Verificação: consulta de áreas e operações do console JPA demonstradas pelo grupo; suítes locais de serviço e repository incluídas no relatório final. Esses resultados históricos não são contagens da versão final.

## 2. Hash, autenticação e permissões

- Problema: substituir identidade simulada por autenticação real sem publicar senhas.
- Prompt do grupo: “vamos para a proxima etapa do projeto, preciso fazer hash ainda hoje”.
- Proposta: BCrypt com PasswordEncoder, cadastro público restrito a estudante/empresa, login/logout com sessão, DTOs seguros e proteção CSRF.
- Decisão observada: o grupo gerou hash, salvou na conta escolhida, implementou as telas e confirmou login e saída. Durante a conferência, enviou o PortalApplication sem o pacote security no scan; a orientação foi incluir esse pacote, após o que o grupo confirmou o login funcionando.
- Mudanças: Senhas, UsuarioAutenticado, PortalUserDetailsService, SecurityConfig, CadastroService, controllers e telas de cadastro/login/conta. Acrescentada inicialização administrativa opt-in para a versão final.
- Verificação: grupo demonstrou identidade autenticada e acesso administrativo negado como estudante; testes automatizados verificam senha inválida, conta sem hash, CSRF, rotação de sessão, logout, rejeição de administrador público e ausência de hash nas respostas.

## 3. Fluxos REST completos e interface mínima

- Problema: concluir os fluxos obrigatórios da Entrega 2 e demonstrar que o backend impede acesso a registros de outras contas.
- Prompt do grupo: “pode fazer esse restante que esta faltando porfavor, faça conforme está escrito no enunciado do projeto, faça testes e me mostre os resultados”.
- Proposta: expor publicação/edição/encerramento/candidatos para empresa, candidatura/inscrições para estudante, áreas para administrador; obter dono da sessão e manter regras no serviço. Criar interface HTML/JavaScript simples e documentação.
- Decisão registrada: o grupo autorizou a implementação completa após enviar o enunciado. A instalação desta versão final no Codespaces e a conferência individual do grupo ainda devem ser registradas pelo grupo; não foram inventadas neste documento.
- Mudanças: EmpresaController, EstudanteController, operações de áreas no AdminController, DTOs, PortalWebService, exceções HTTP apropriadas, InicializadorAdministrador e portal.html/portal.js. Atualizados README, API, diagramas, roteiro e relatório.
- Verificação do assistente: 58 testes sem banco e 33 de integração local aprovados; fluxos Chromium de empresa e estudante, isolamento, administração e persistência após reiniciar aprovados. Ver RESULTADOS_ENTREGA_2.md. O Supabase do grupo não foi alterado pelo assistente.

## Preparação individual

Cada integrante deve explicar uma associação, a função dos pacotes, o fluxo de candidatura, a obtenção da identidade autenticada, o papel do CSRF e uma regra de propriedade. Registrar as decisões adicionais com palavras próprias e praticar uma pequena alteração no laboratório sem IA.

# Checklist da Entrega 2 — conforme enunciado enviado

## Implementado e verificado localmente

- [x] Spring Boot, PostgreSQL, JPA e camadas com dependências por construtor.
- [x] Cadastro público apenas de estudante/empresa, e-mail único e BCrypt.
- [x] Login/logout por sessão; identidade obtida da autenticação.
- [x] CSRF e autorização por perfil e propriedade no backend.
- [x] Consulta pública de vagas abertas com filtros por área/modalidade.
- [x] Empresa: publicar, listar próprias, editar, encerrar e consultar candidatos.
- [x] Estudante: candidatar e consultar somente próprias candidaturas.
- [x] Administrador: cadastrar/editar/excluir áreas e consultar contagens.
- [x] Inicialização administrativa controlada sem cadastro público ou senha padrão.
- [x] DTOs, validação, erros HTTP e respostas sem senha/hash/detalhes internos.
- [x] Interface mínima em HTML/JavaScript, inclusive candidatura, próprias inscrições e operações da empresa.
- [x] Testes das regras obrigatórias e acesso direto com perfil/propriedade indevidos.
- [x] Persistência após reiniciar, confirmada localmente.
- [x] Documentação da API, exemplos, diagramas atualizados, SQL/dados fictícios e relatório.
- [x] Três registros de interações reais com IA preparados para conferência do grupo.

## Ações finais do grupo

- [ ] Instalar o pacote completo no checkout do grupo e executar mvn clean test (58 testes).
- [ ] Repetir o roteiro de demonstração no ambiente que será apresentado, com suas variáveis de banco.
- [ ] Preparar e conferir a conta administrativa de demonstração pelo procedimento controlado.
- [ ] Revisar os registros de IA, diagramas e explicações; todos devem conseguir explicar e realizar pequena alteração sem IA.
- [ ] Criar a versão entrega-2 no Git, conferir a ausência de credenciais e arquivos de compilação e publicar o link correto.
- [ ] Enviar link e ZIP no Moodle. O assistente não realizou commit/push/envio.

O relatório local não deve ser apresentado como teste feito pelo assistente no Supabase do grupo. Não são necessários React, JWT, gráficos, chat, hospedagem ou integrações externas para cumprir o escopo obrigatório.

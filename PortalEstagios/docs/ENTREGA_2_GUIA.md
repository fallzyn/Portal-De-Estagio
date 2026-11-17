# Instalar a versão completa e conferir a Entrega 2

## Copiar os arquivos

O pacote completo contém a pasta PortalEstagios com código, testes, configuração, páginas, SQL e documentação. Faça uma cópia de segurança dos seus arquivos atuais e extraia o pacote na raiz do repositório, mesclando PortalEstagios com a pasta já existente. Evite criar PortalEstagios/PortalEstagios. Substitua os arquivos de mesmo caminho e acrescente os novos. Preserve registros próprios do grupo quando integrar a documentação.

O pacote não contém src/dao, substituído por repository na migração para JPA. Se essa pasta antiga ainda existir no seu checkout, remova suas classes JDBC obsoletas, conforme a etapa 2B; as duas implementações não devem ser combinadas. Não remova src/repository. Não copie a pasta target antiga.

Confira especialmente:

- pom.xml e resources/application.properties;
- src/app/PortalApplication.java, incluindo os pacotes config e security;
- src/controller, src/dto, src/security, src/service, src/config;
- resources/static, incluindo portal.html e js/portal.js;
- todos os arquivos de tests, inclusive FluxosRestIT.java e InicializacaoAdminTest.java.

A estrutura mantém src, resources e tests diretamente sob PortalEstagios. Não crie uma segunda estrutura src/main/java. Todos os nomes de arquivo Java devem coincidir com a classe pública e não têm acentos.

## Compilar e iniciar

1. No diretório PortalEstagios, execute `mvn clean test`. Esperado: 58 testes sem falhas, erros ou ignorados.
2. Configure DB_URL, DB_USER e DB_PASSWORD conforme o README. O Supabase existente com senha_hash não exige novo SQL.
3. Execute `mvn spring-boot:run`. Espere a mensagem Started PortalApplication.
4. Abra /portal.html na URL da porta 8080.
5. Prepare o administrador somente pelo procedimento explícito do README. Não existe senha padrão e o formulário público não cria administradores.

## Roteiro obrigatório com autenticação real

Use apenas dados fictícios no banco escolhido. As operações feitas na aplicação gravam no banco indicado em DB_URL. Para contas distintas, use perfis separados do navegador ou janelas privativas; cada perfil mantém seu próprio cookie. Não use a identidade simulada do console para comprovar segurança.

1. Crie empresa A e empresa B com e-mails distintos e senhas escolhidas por você. Crie estudante A e estudante B. Repita um e-mail: deve rejeitar sem criar outra conta.
2. Entre com a empresa A e abra /portal.html. Publique vaga com área existente e carga positiva. Confira a listagem própria. Tente título vazio ou carga zero: o formulário bloqueia; requisição direta equivalente deve retornar 400.
3. Edite a vaga e confirme a atualização. Entre como empresa B: a vaga de A não aparece na lista própria. Acesso direto de B para editar, encerrar ou consultar candidatos de A deve retornar 403. Os endpoints e exemplos estão em API.md.
4. Consulte as vagas sem login e aplique os filtros. Uma combinação sem registros retorna lista vazia. Uma modalidade desconhecida retorna 400.
5. Entre como estudante A e clique Candidatar-se. A vaga deve aparecer em Minhas candidaturas. Repetir deve exibir Candidatura já existente, com 409 na API.
6. Entre como estudante B: não deve ver a inscrição de A. A API não recebe um ID de estudante para escolher o dono; campos adicionais no JSON são rejeitados.
7. Empresa A consulta candidatos e encontra somente inscritos de sua vaga. Encerre a vaga. Ela desaparece da consulta pública, permanece na listagem da empresa e preserva as candidaturas anteriores. Estudante B não consegue se inscrever nela, mesmo usando uma página antiga ou chamando diretamente a API (409).
8. Como administrador, cadastre e edite uma área livre, depois exclua (201/204/204). Tente excluir uma área usada por vaga: 409. Consulte /api/admin/contagens. Como estudante ou empresa, a mesma rota retorna 403; sem login, 401.
9. Clique em Sair pela página Minha conta. /conta.html deve levar ao login; /api/auth/eu deve retornar 401. Senha incorreta exibe mensagem genérica. POST autenticado sem token CSRF retorna 403.
10. Cadastro público com perfil ADMINISTRADOR ou campos extras de privilégio deve retornar 400. Respostas de cadastro, identidade, vaga e candidatura não devem conter senha nem hash.
11. Pare o servidor, inicie de novo e faça novo login. A vaga e as inscrições devem continuar no banco, inclusive se encerradas. As sessões anteriores não persistem entre reinicializações.

As funções administrativas podem ser demonstradas com curl ou cliente de API; não é exigida tela administrativa. As telas mínimas do enunciado estão disponíveis sem frameworks adicionais.

## Entender o código para a apresentação

- EmpresaController usa o ID de UsuarioAutenticado, nunca um ID de proprietário enviado no corpo.
- PortalWebService mantém a montagem do DTO na transação; PortalService continua dono das regras.
- PortalService.propria verifica empresa e propriedade; candidatar verifica estudante, situação e duplicidade sob bloqueio da vaga.
- Candidatura representa a relação muitos-para-muitos com data própria. Seu DTO tem os dados da vaga, sem retornar o estudante inteiro.
- InicializadorAdministrador é opt-in e chama o serviço transacional; não oferece uma rota pública de criação de administrador.
- portal.js usa textContent para mostrar dados e busca token CSRF antes de cada gravação. A sessão é enviada por cookie; não há senha ou JWT em localStorage.
- SecurityConfig protege prefixos no backend e mantém CSRF. Esconder um botão por perfil só melhora a interface; não substitui a autorização.

Consulte RESULTADOS_ENTREGA_2.md para as evidências locais. O grupo deve executar esse roteiro no próprio ambiente e registrar seus resultados antes de entregar.

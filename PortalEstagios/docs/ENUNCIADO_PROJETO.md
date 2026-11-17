PROGRAMAÇÃO DE SISTEMAS II · Projeto 2026/2

Portal de Estágios
O grupo desenvolverá um portal que conecta estudantes a oportunidades de estágio. O projeto começa em 14/10/2026 e será construído em duas entregas: uma aplicação Java com persistência e, depois, uma aplicação web com API REST e autenticação usando Spring Security.

A IA será uma ferramenta de desenvolvimento, mas a responsabilidade pelo sistema é do grupo. Cada integrante deverá entender o código, explicar a arquitetura e realizar pequenas alterações durante o acompanhamento e a avaliação.

1. Objetivos de aprendizagem
Modelar o domínio e representar suas associações em Java e no banco de dados.
Separar apresentação, regras de negócio e persistência em camadas.
Aplicar interfaces, orientação a objetos, exceções, JDBC e DAO.
Evoluir a aplicação para serviços REST com autenticação e autorização.
Usar IA para construir e revisar soluções que o grupo consiga explicar e verificar.
2. Escopo e regras de negócio
O portal terá os perfis estudante, empresa e administrador. Uma conta de empresa representa uma empresa; não é necessário implementar vários representantes.

O modelo deverá representar, no mínimo:

Usuário: identificador, nome, e-mail único e perfil. Registrar curso para estudante e nome da empresa para empresa. A senha será acrescentada na Entrega 2.
Área: identificador e nome único, como tecnologia, administração ou design.
Vaga: identificador, empresa responsável, título, descrição, área, localização, modalidade (presencial, remota ou híbrida), carga horária semanal positiva e situação (aberta ou encerrada).
Candidatura: identificador, estudante, vaga e data da inscrição.
Uma empresa pode publicar várias vagas; uma vaga pode receber várias candidaturas; um estudante pode se candidatar a várias vagas. A candidatura representa a associação entre estudante e vaga. O grupo poderá adaptar a estrutura de classes, desde que preserve essas informações e justifique a escolha.

Somente empresas podem publicar, editar e encerrar suas próprias vagas.
Somente estudantes podem se candidatar. A vaga deve estar aberta e a inscrição não pode ser repetida para o mesmo estudante e vaga.
O estudante consulta apenas suas candidaturas. A empresa consulta candidatos apenas das suas vagas.
Somente administradores cadastram e editam áreas. Uma área usada por uma vaga não pode ser excluída.
Encerrar uma vaga impede novas candidaturas e preserva as anteriores. A reabertura não faz parte do escopo obrigatório.
A consulta pública apresenta vagas abertas e permite filtrar por área e modalidade.
O administrador consulta as quantidades de estudantes, empresas, vagas abertas, vagas encerradas e candidaturas.
Chat, notificações, recomendações por IA, integrações externas, gráficos, hospedagem e SPA com React, Angular ou Vue são opcionais e não substituem os requisitos obrigatórios.

3. Atividade inicial: 14/10
O grupo deverá começar pela modelagem e pela criação do projeto básico em Java. O objetivo do encontro é estabelecer uma base compreensível para o desenvolvimento.

Analisar o enunciado: identificar os perfis, as operações e as regras. Registrar dúvidas para discutir com o professor.
Modelar o domínio: elaborar um diagrama com classes, atributos, associações e multiplicidades. Identificar quem é responsável por cada informação e quais dados precisam ser únicos.
Planejar as camadas: descrever as responsabilidades de apresentação, modelo, serviço e DAO e o caminho de uma operação de candidatura.
Criar o projeto Java: configurar o ambiente e o repositório, criar os pacotes app, model, service e dao e uma classe Main executável.
Criar as classes iniciais: implementar os atributos e construtores essenciais do modelo e os enums necessários, como perfil, modalidade e situação da vaga.
Executar uma demonstração simples: na classe principal, criar objetos fictícios de empresa, estudante, área e vaga, relacioná-los e imprimir informações para comprovar que o projeto compila e executa.
Registrar o próximo passo: atualizar o README com os integrantes, instruções de execução e tarefas previstas para a persistência e as regras de negócio.
Ao final do laboratório: apresentar o modelo, executar o projeto básico e explicar uma associação e a função de cada pacote. Registrar o primeiro commit e uma interação relevante com a IA. JDBC, banco de dados, API REST e autenticação serão desenvolvidos nas etapas seguintes; não são exigidos nesta atividade inicial.

O grupo pode pedir à IA uma revisão do modelo ou explicações sobre alternativas. Antes de gerar as classes, deve revisar a proposta e conseguir justificar as decisões com suas próprias palavras.

4. Arquitetura esperada
Entrega 1: menu no console → serviço → interface DAO → implementação JDBC → banco.
Entrega 2: requisição HTTP → segurança → controller → serviço → DAO/repository → banco.

Model: representa os elementos e as associações do domínio.
Service: aplica regras de negócio e coordena as operações.
DAO/repository: consulta e grava dados, sem concentrar as regras de negócio.
Apresentação/controller: recebe entradas e apresenta respostas; não contém SQL.
DTO e segurança, na Entrega 2: controlam os dados expostos e o acesso às operações.
As dependências devem ser explícitas, preferencialmente recebidas pelo construtor. Use interfaces para responsabilidades claras, como persistência. Herança e classes abstratas devem ser discutidas quando fizerem sentido; não é obrigatório representar os perfis por herança.

5. Entrega 1: Java, JDBC e DAO
Data: 28/10/2026. Entregar uma aplicação executável pelo console, com os fluxos obrigatórios e persistência em PostgreSQL por JDBC.

Cadastrar estudantes e empresas; cadastrar, listar, editar e excluir áreas conforme as regras.
Publicar, consultar, filtrar, editar e encerrar vagas.
Realizar candidaturas, consultar as próprias candidaturas e listar candidatos de uma vaga da empresa.
Apresentar as contagens administrativas.
Separar menu, serviços, interfaces DAO e implementações JDBC.
Usar consultas parametrizadas, fechar recursos e tratar entradas inválidas e falhas de persistência sem encerrar inesperadamente a aplicação.
Definir chaves estrangeiras e restrições de unicidade para e-mail e para o par estudante–vaga.
Aplicar uma interface funcional ou expressão lambda em uma operação útil e explicar sua finalidade.
Identidade simulada: o menu permitirá selecionar um usuário de demonstração cadastrado. Os serviços validarão o perfil e a propriedade dos registros. Isso não é autenticação; será substituído pelo usuário autenticado na Entrega 2. Não é necessário armazenar senhas nesta etapa.

Enviar no Moodle: link do repositório e ZIP da versão entrega-1, README com integrantes e instruções, scripts SQL de criação e dados fictícios, modelo de domínio e esquema das tabelas, explicação das camadas, registro de IA e roteiro de verificação. Não incluir pastas de compilação ou credenciais reais.

6. Entrega 2: API REST e Spring Security
Data: 17/11/2026. Evoluir o mesmo projeto para Spring Boot, preservando as regras de negócio e acrescentando API REST, interface simples, autenticação e autorização.

Disponibilizar os fluxos obrigatórios por endpoints REST com métodos HTTP e códigos de resposta coerentes.
Manter PostgreSQL. Reaproveitar JDBC/DAO ou migrar para JPA/repository após discutir a decisão com o professor, preservando as regras nos serviços.
Usar DTOs, validar entradas e padronizar erros. Não expor senhas, hashes ou detalhes internos de exceções.
Implementar cadastro de estudante e empresa, login e logout com Spring Security. O cadastro público não pode criar administradores nem aceitar elevação de privilégios.
Armazenar senhas com hash unidirecional usando PasswordEncoder, por exemplo BCrypt.
Usar autenticação por sessão como referência. JWT não é obrigatório; outra abordagem deve ser discutida e explicada.
Obter a identidade a partir da autenticação, sem confiar em um identificador enviado pelo cliente para definir o proprietário de uma operação.
Aplicar autorização no backend por perfil e por propriedade dos registros, inclusive no acesso direto às URLs.
Manter proteção CSRF nas operações autenticadas por sessão e explicar como o cliente envia o token. Configurar acesso entre origens somente quando necessário.
Documentar os endpoints, exemplos de requisições, respostas e autenticação no README ou em OpenAPI.
Interface mínima: páginas de login/logout, consulta de vagas, candidatura e consulta das próprias candidaturas, além de publicação, encerramento e consulta de candidatos pela empresa. Pode usar HTML com JavaScript ou Thymeleaf. As funções administrativas podem ser demonstradas por cliente de API.

Enviar no Moodle: link e ZIP da versão entrega-2, instruções para executar aplicação e banco, diagramas atualizados, documentação da API, dados fictícios, registro de IA e evidências de verificação. Explicar o que foi reaproveitado ou alterado entre as entregas. A conta administrativa de demonstração deve ser criada por inicialização controlada, sem cadastro público.

A avaliação do projeto será em 18/11/2026, no laboratório, sobre a versão entregue em 17/11.

7. Uso orientado de IA
Trabalhem em incrementos pequenos: formulem o problema, solicitem uma proposta, revisem o código, executem os cenários e registrem a decisão antes de avançar. Peçam explicações sobre responsabilidades, dependências, alternativas e falhas possíveis.

Registrem pelo menos três interações relevantes por entrega: problema, ferramenta e prompt, proposta da IA, decisão do grupo, mudanças realizadas e forma de verificação. Não é necessário guardar todas as conversas. Usem dados fictícios e não enviem credenciais reais.

Todos devem explicar a finalidade, entradas, saídas e dependências do código incorporado. Na avaliação individual, a explicação e a pequena alteração serão feitas sem consulta à IA.

8. Acompanhamento e calendário
Nos encontros do projeto, levem uma versão executável, uma decisão para explicar, uma dificuldade e o próximo passo. O professor poderá escolher um integrante para acompanhar uma operação no código.

Calendário do projeto
Data	Atividade
14/10	Início: modelagem, organização das camadas e criação do projeto básico em Java.
21/10	Acompanhamento de JDBC/DAO, regras de negócio, exceções e verificação da Entrega 1.
28/10	Entrega 1 e Ativ2, conforme o plano de aulas.
04/11	API REST com SpringBoot, migração do código e primeiros controladores
11/11	API REST, integração da interface, Spring Security e autorização por perfil e propriedade.
17/11	Entrega 2: envio da versão final integrada, executável e documentada.
18/11	Avaliação do projeto: demonstração, explicação da arquitetura e pequena alteração individual.
Planejem o estudo orientado e o desenvolvimento entre os encontros. A segurança deverá estar integrada na entrega de 17/11. As datas da Entrega 2 e da avaliação estão trocadas em relação ao plano de aulas original.

9. Verificação obrigatória
Registrem entradas, resultado esperado e resultado observado. Na Entrega 1, usem perfis simulados; na Entrega 2, autenticação real.

Cadastro válido e rejeição de e-mail repetido.
Vaga válida e rejeição de campos ausentes ou carga horária inválida.
Consulta de vagas abertas com filtros.
Candidatura válida, rejeição de duplicidade e rejeição em vaga encerrada.
Bloqueio de edição, encerramento e consulta de candidatos de vaga de outra empresa.
Bloqueio de consulta às candidaturas de outro estudante.
Rejeição da exclusão de área utilizada e manutenção dos dados após reiniciar a aplicação.
Na Entrega 2: login, senha inválida, logout e bloqueio de acesso sem autenticação ou com perfil inadequado.
Na Entrega 2: cadastro público sem criação de administrador e respostas sem senhas ou hashes.
Automatizem ao menos três testes de regras: candidatura duplicada, vaga encerrada e acesso a vaga de outra empresa. Os demais cenários podem ser verificados por roteiro manual ou coleção de requisições.



Avaliação: a entrega 1 sera avaliada 100% baseada na entrega feita pelo grupo. A entrega 2 será 30% da entrega e 70% da avaliação. 
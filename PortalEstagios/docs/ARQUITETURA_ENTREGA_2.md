# Arquitetura da Entrega 2

```mermaid
flowchart LR
    N[Navegador HTML e JavaScript] --> S[Spring Security: sessão, CSRF, perfil]
    S --> C[Controllers e validação dos DTOs]
    C --> W[PortalWebService: DTO dentro da transação]
    W --> P[PortalService: regras e propriedade]
    C --> P
    P --> R[Repositories Spring Data JPA]
    R --> H[Hibernate]
    H --> B[(PostgreSQL)]
    E[Exceções de negócio] --> A[ApiErros: HTTP e JSON seguros]
    P --> E
```

As dependências são recebidas por construtor. Model contém entidades e enums; service coordena regras e transações; repository concentra persistência; controller recebe entradas HTTP e usa a identidade autenticada. DTOs selecionam os dados expostos. O HTML não tem SQL e os repositories não decidem perfil/propriedade.

```mermaid
sequenceDiagram
    actor Estudante
    participant Tela as portal.js
    participant Seg as Spring Security
    participant C as EstudanteController
    participant W as PortalWebService
    participant P as PortalService
    participant DB as PostgreSQL/JPA
    Estudante->>Tela: Candidatar-se
    Tela->>Seg: GET /api/auth/csrf com cookie
    Seg-->>Tela: Token CSRF
    Tela->>Seg: POST candidatura com cookie, token e vagaId
    Seg->>C: Perfil ESTUDANTE validado
    C->>W: candidatar(id da sessão, vagaId)
    W->>P: candidatar(estudante, vaga)
    P->>DB: Buscar estudante e bloquear vaga
    P->>P: Validar perfil, aberta e não duplicada
    P->>DB: Gravar candidatura
    DB-->>P: Candidatura persistida
    P-->>W: Entidade
    W-->>C: DTO seguro ainda dentro da transação
    C-->>Tela: HTTP 201 com candidatura
    Tela-->>Estudante: Inscrição registrada
```

O bloqueio de linha da vaga coordena candidatura, edição e encerramento. A restrição UNIQUE do par estudante-vaga reforça a unicidade. Consultas próprias sempre usam a sessão; IDs enviados pelo cliente não definem o proprietário. O serviço verifica propriedade mesmo após a autorização por perfil.

Migração: o console/JDBC da Entrega 1 foi preservado como histórico e evoluído para JPA. O console legado ainda pode executar as regras, mas a identidade simulada não faz parte da autenticação da Entrega 2. Não há JWT obrigatório, SPA, chat, gráficos ou integrações externas; a interface mínima usa HTML/JavaScript, como permitido no enunciado.

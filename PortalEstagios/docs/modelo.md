# Modelo de domínio e esquema — Entrega 2

```mermaid
classDiagram
    class Usuario {
        Long id
        String nome
        String email
        Perfil perfil
        String curso
        String nomeEmpresa
        String senhaHash
    }
    class Area {
        Long id
        String nome
    }
    class Vaga {
        Long id
        String titulo
        String descricao
        String localizacao
        Modalidade modalidade
        int cargaHoraria
        Situacao situacao
    }
    class Candidatura {
        Long id
        LocalDateTime dataInscricao
    }
    Usuario "1" --> "0..*" Vaga : empresa responsável
    Usuario "1" --> "0..*" Candidatura : estudante
    Area "1" --> "0..*" Vaga : classificação
    Vaga "1" --> "0..*" Candidatura : inscrições
```

O perfil restringe o papel do usuário: empresa possui nomeEmpresa, estudante possui curso, administrador não possui os dois. Não é necessária herança por perfil. Uma candidatura representa a associação entre estudante e vaga e guarda sua própria data em UTC. Encerrar uma vaga preserva as inscrições.

```mermaid
erDiagram
    usuarios ||--o{ vagas : empresa_id
    usuarios ||--o{ candidaturas : estudante_id
    areas ||--o{ vagas : area_id
    vagas ||--o{ candidaturas : vaga_id
    usuarios {
        bigint id PK
        varchar nome
        varchar email UK
        varchar perfil
        varchar curso
        varchar nome_empresa
        varchar senha_hash
    }
    areas {
        bigint id PK
        varchar nome UK
    }
    vagas {
        bigint id PK
        bigint empresa_id FK
        varchar titulo
        text descricao
        bigint area_id FK
        varchar localizacao
        varchar modalidade
        integer carga_horaria
        varchar situacao
    }
    candidaturas {
        bigint id PK
        bigint estudante_id FK
        bigint vaga_id FK
        timestamp data_inscricao
    }
```

Restrições adicionais: UNIQUE(estudante_id, vaga_id); carga_horaria positiva; enum de perfis, modalidades e situações; área usada tem exclusão restringida pela FK; e-mail e nome de área únicos. senha_hash é nullable para preservar contas antigas, mas contas sem hash válido não autenticam. O hash tem 60 caracteres BCrypt e nunca é exposto nos DTOs.

O esquema é criado por sql/01_criacao.sql e recebe senha_hash por sql/03_hash_senha.sql. As entidades @ManyToOne usam as chaves estrangeiras existentes. As regras de perfil e propriedade são do serviço, além das restrições do banco e das autorizações HTTP.

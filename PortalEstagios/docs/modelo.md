# Modelo de domínio e esquema

```text
Usuario (id, nome, email UNIQUE, perfil, curso?, nomeEmpresa?)
  1 Empresa ───── 0..* Vaga
  1 Estudante ─── 0..* Candidatura
Area (id, nome UNIQUE) 1 ───── 0..* Vaga
Vaga (id, empresaId, titulo, descricao, areaId, localizacao, modalidade, cargaHoraria, situacao)
  1 Vaga ───── 0..* Candidatura
Candidatura (id, estudanteId, vagaId, dataInscricao)
  UNIQUE (estudanteId, vagaId)
```

Cada candidatura pertence a um estudante e a uma vaga. A vaga pertence a uma empresa e a uma área. Os identificadores são chaves estrangeiras no banco. Uma área usada não pode ser excluída. Uma vaga encerrada mantém candidaturas existentes.

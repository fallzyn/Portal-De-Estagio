CREATE TABLE usuarios (
 id BIGSERIAL PRIMARY KEY,
 nome VARCHAR(150) NOT NULL CHECK (length(trim(nome))>0),
 email VARCHAR(200) NOT NULL UNIQUE,
 perfil VARCHAR(20) NOT NULL CHECK (perfil IN ('ESTUDANTE','EMPRESA','ADMINISTRADOR')),
 curso VARCHAR(150), nome_empresa VARCHAR(150),
 CONSTRAINT dados_perfil CHECK ((perfil='ESTUDANTE' AND curso IS NOT NULL AND nome_empresa IS NULL) OR (perfil='EMPRESA' AND nome_empresa IS NOT NULL AND curso IS NULL) OR (perfil='ADMINISTRADOR' AND curso IS NULL AND nome_empresa IS NULL))
);
CREATE TABLE areas(id BIGSERIAL PRIMARY KEY,nome VARCHAR(100) NOT NULL UNIQUE);
CREATE TABLE vagas (
 id BIGSERIAL PRIMARY KEY,
 empresa_id BIGINT NOT NULL REFERENCES usuarios(id),
 titulo VARCHAR(150) NOT NULL CHECK(length(trim(titulo))>0),
 descricao TEXT NOT NULL CHECK(length(trim(descricao))>0),
 area_id BIGINT NOT NULL REFERENCES areas(id) ON DELETE RESTRICT,
 localizacao VARCHAR(150) NOT NULL CHECK(length(trim(localizacao))>0),
 modalidade VARCHAR(20) NOT NULL CHECK(modalidade IN ('PRESENCIAL','REMOTA','HIBRIDA')),
 carga_horaria INTEGER NOT NULL CHECK(carga_horaria>0),
 situacao VARCHAR(20) NOT NULL CHECK(situacao IN ('ABERTA','ENCERRADA'))
);
CREATE TABLE candidaturas (
 id BIGSERIAL PRIMARY KEY,
 estudante_id BIGINT NOT NULL REFERENCES usuarios(id),
 vaga_id BIGINT NOT NULL REFERENCES vagas(id),
 data_inscricao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(estudante_id,vaga_id)
);
-- Regras de perfil e situação são validadas pelo serviço Java.

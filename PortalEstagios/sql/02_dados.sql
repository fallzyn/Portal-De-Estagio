INSERT INTO usuarios(nome,email,perfil) VALUES ('Administrador','admin@exemplo.com','ADMINISTRADOR');
INSERT INTO usuarios(nome,email,perfil,curso) VALUES ('Ana Estudante','ana@exemplo.com','ESTUDANTE','Sistemas de Informação');
INSERT INTO usuarios(nome,email,perfil,nome_empresa) VALUES ('Empresa Exemplo','empresa@exemplo.com','EMPRESA','Tech Exemplo');
INSERT INTO areas(nome) VALUES ('Tecnologia'),('Administração'),('Design');
INSERT INTO vagas(empresa_id,titulo,descricao,area_id,localizacao,modalidade,carga_horaria,situacao)
SELECT u.id,'Estágio em Desenvolvimento','Apoio à equipe de desenvolvimento',a.id,'São Paulo', 'HIBRIDA',30,'ABERTA' FROM usuarios u CROSS JOIN areas a WHERE u.email='empresa@exemplo.com' AND a.nome='Tecnologia';

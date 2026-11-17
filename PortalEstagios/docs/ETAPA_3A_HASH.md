# Etapa 3A — hash BCrypt

Esta etapa prepara hashes e armazenamento. Ainda não implementa login, sessão, logout nem cadastro web com senha. O console antigo ainda permite identidade simulada e os cadastros antigos continuam sem hash até a definição individual. A etapa 3B deverá exigir senha no novo cadastro e bloquear login de contas com hash nulo.

BCrypt é uma função de hash de senha, não criptografia reversível. `encode` produz um hash com salt aleatório; `matches` confere a senha digitada contra o hash armazenado. A implementação usa fator 12. Dois hashes da mesma senha podem ser diferentes e ambos conferirem corretamente. A política inicial exige pelo menos 8 caracteres e no máximo 72 bytes UTF-8, pois BCrypt tem esse limite; prefira uma senha longa e exclusiva.

## 1. Preparar a coluna no Supabase

Pare o servidor Java com Ctrl+C. No SQL Editor, execute apenas o arquivo `sql/03_hash_senha.sql`, em nova consulta. Não repita os scripts 01/02. O script acrescenta `senha_hash` e uma restrição de formato BCrypt, sem definir senhas, remover usuários ou substituir hashes existentes. Pode ser repetido.

Confira sem exibir hashes:

```sql
SELECT id, nome, email, perfil, senha_hash IS NOT NULL AS senha_definida
FROM public.usuarios ORDER BY id;
```

Contas antigas devem continuar com `senha_definida=false`. Não coloque uma mesma senha para todo mundo.

## 2. Aplicar os arquivos

Copie os caminhos do pacote desta etapa, preservando o que tiver alterações próprias:

- `pom.xml`: inclui apenas `spring-security-crypto` e permite escolher o main do plugin Exec pela propriedade `exec.mainClass`. Não ativa automaticamente uma tela de login.
- `src/model/Usuario.java`: acrescenta o campo privado `senhaHash`, excluído da serialização e do `toString`. O método que o define recusa texto que não tenha formato de hash BCrypt.
- `src/security/Senhas.java`: gera e valida o hash, aplicando os limites da senha.
- `src/config/SenhaConfig.java`: disponibiliza um bean `PasswordEncoder` para a próxima etapa.
- `src/app/PortalApplication.java`: acrescenta `config` à descoberta do Spring.
- `src/app/GerarHash.java`: ferramenta local que gera o hash, sem iniciar Spring ou acessar banco.
- `tests/SenhasTest.java`: verifica BCrypt, salt, senha incorreta e validação de limites/formato.

O hash é informação sensível de autenticação: não publique no Git, em prints ou no chat. Não escreva a senha no código ou nos argumentos do Maven.

## 3. Testar o código

Dentro de `PortalEstagios`:

```bash
mvn clean test
```

São 29 testes comuns esperados (os 25 anteriores e 4 novos). A integração PostgreSQL requer aplicar também o script 03 ao banco local exclusivo de testes antes de executar `mvn clean verify -Pintegracao`.

## 4. Gerar o hash no seu Codespaces

No Bash, dentro de `PortalEstagios`:

```bash
read -rsp 'Escolha a senha DE LOGIN da conta: ' PORTAL_SENHA_HASH
echo
export PORTAL_SENHA_HASH
mvn compile exec:java "-Dexec.mainClass=app.GerarHash"
unset PORTAL_SENHA_HASH
```

A senha não aparece durante a digitação. Essa senha de login é diferente de `DB_PASSWORD`, que conecta a aplicação ao banco. A ferramenta imprime o hash, após conferir localmente `matches(senha, hash)`. Ela não salva nem imprime a senha e não modifica o Supabase. No uso pelo Maven, a variável é necessária porque `System.console()` geralmente está indisponível; quando disponível, a ferramenta também suporta a leitura oculta pelo console Java.

Você não precisa configurar a conexão DB para gerar esse hash. Não gere hashes em sites externos.

## 5. Definir a senha de UMA conta existente

No SQL Editor do Supabase, selecione a conta pelo e-mail e use o hash que acabou de gerar:

```sql
UPDATE public.usuarios
SET senha_hash = 'COLE_AQUI_O_HASH_BCRYPT_COMPLETO'
WHERE email = 'EMAIL_DA_CONTA_ESCOLHIDA'
  AND senha_hash IS NULL
RETURNING id, email, senha_hash IS NOT NULL AS senha_definida;
```

Substitua os dois exemplos antes de executar. Use o hash completo começando por `$2a$12$` (60 caracteres). A senha original nunca vai para esse SQL. O filtro `senha_hash IS NULL` evita sobrescrever uma senha já definida. O e-mail é único, então o UPDATE deve retornar uma conta. Se retornar zero, confira o e-mail e se já havia senha definida; não remova a condição para forçar uma troca.

Este é um procedimento administrativo inicial, executado por quem já tem acesso ao banco. Não será disponibilizado como endpoint público. Alteração/recuperação de senha precisará de um fluxo autorizado posterior.

## Checkpoint

Confirme os testes e uma conta com `senha_definida=true`. Não me envie a senha nem o hash. As outras contas podem permanecer com hash nulo até serem tratadas individualmente. O servidor continua usando `ddl-auto=validate`, então o script 03 precisa ser aplicado antes de iniciar a nova versão conectada ao Supabase.

Depois implementaremos cadastro com senha, login por sessão e logout com Spring Security. Guardar hash sozinho não torna a seleção de usuário do console uma autenticação real.

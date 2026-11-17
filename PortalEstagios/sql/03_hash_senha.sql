-- Etapa 3A: aplicar uma vez no banco já existente. Repetir é seguro.
-- Não define senhas, não remove dados e não altera as contas já configuradas.
BEGIN;
ALTER TABLE public.usuarios ADD COLUMN IF NOT EXISTS senha_hash VARCHAR(60);
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.usuarios'::regclass AND conname = 'usuarios_senha_hash_bcrypt'
    ) THEN
        ALTER TABLE public.usuarios ADD CONSTRAINT usuarios_senha_hash_bcrypt
        CHECK (senha_hash IS NULL OR senha_hash ~ '^\$2[aby]\$(0[4-9]|[12][0-9]|3[01])\$[./A-Za-z0-9]{53}$');
    END IF;
END $$;
COMMIT;

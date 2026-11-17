package app;

import java.io.Console;
import java.util.Arrays;
import security.Senhas;

// Ferramenta local: não inicia servidor, não conecta ao banco e não altera contas.
public class GerarHash {
    public static void main(String[] args) {
        Console console = System.console();
        char[] digitada = console == null ? null : console.readPassword("Senha da conta: ");
        try {
            String senha = digitada == null ? System.getenv("PORTAL_SENHA_HASH") : new String(digitada);
            var encoder = Senhas.encoder();
            String hash = Senhas.gerarHash(senha, encoder);
            if (!encoder.matches(senha, hash)) throw new IllegalStateException("Falha ao conferir o hash.");
            System.out.println("Hash BCrypt conferido localmente (guarde a senha, não envie ao chat):");
            System.out.println(hash);
        } finally {
            if (digitada != null) Arrays.fill(digitada, '\0');
        }
    }
}
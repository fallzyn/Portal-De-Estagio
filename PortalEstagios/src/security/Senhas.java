package security;

import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class Senhas {
    private Senhas() { }

    public static PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(12);
    }

    public static String gerarHash(String senha, PasswordEncoder encoder) {
        if (senha == null || senha.isBlank() || senha.codePointCount(0, senha.length()) < 8) {
            throw new IllegalArgumentException("Use uma senha com pelo menos 8 caracteres.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("A senha excede o limite de 72 bytes do BCrypt.");
        }
        return encoder.encode(senha);
    }

    public static boolean formatoBcryptValido(String hash) {
        return hash != null && hash.matches("^\\$2[aby]\\$(0[4-9]|[12][0-9]|3[01])\\$[./A-Za-z0-9]{53}$");
    }
}

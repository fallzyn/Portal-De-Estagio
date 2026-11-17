import config.SenhaConfig;
import model.*;
import org.junit.jupiter.api.Test;
import security.Senhas;
import static org.junit.jupiter.api.Assertions.*;

class SenhasTest {
    @Test void geraHashEConfereSenhaCorretaEIncorreta() {
        var encoder = new SenhaConfig().passwordEncoder();
        String senha = "SomenteTeste#123";
        String hash = Senhas.gerarHash(senha, encoder);
        assertEquals(60, hash.length());
        assertTrue(hash.startsWith("$2a$12$"));
        assertNotEquals(senha, hash);
        assertTrue(encoder.matches(senha, hash));
        assertFalse(encoder.matches("OutraSenha#123", hash));
    }

    @Test void saltGeraHashesDiferentesParaAMesmaSenha() {
        var encoder = Senhas.encoder();
        String a = Senhas.gerarHash("SomenteTeste#123", encoder);
        String b = Senhas.gerarHash("SomenteTeste#123", encoder);
        assertNotEquals(a, b);
        assertTrue(encoder.matches("SomenteTeste#123", a));
        assertTrue(encoder.matches("SomenteTeste#123", b));
    }

    @Test void rejeitaSenhaAusenteCurtaOuQueExcedaBytesBcrypt() {
        var encoder = Senhas.encoder();
        assertThrows(IllegalArgumentException.class, () -> Senhas.gerarHash(null, encoder));
        assertThrows(IllegalArgumentException.class, () -> Senhas.gerarHash("1234567", encoder));
        assertThrows(IllegalArgumentException.class, () -> Senhas.gerarHash(" ".repeat(8), encoder));
        assertThrows(IllegalArgumentException.class, () -> Senhas.gerarHash("á".repeat(37), encoder));
    }

    @Test void entidadeNaoAceitaSenhaEmTextoNemExibeHashNoConsole() {
        Usuario usuario = new Usuario(1, "Teste", "teste@exemplo.com", Perfil.ESTUDANTE, "SI", null);
        assertNull(usuario.senhaHash());
        assertThrows(IllegalArgumentException.class, () -> usuario.definirSenhaHash("SenhaEmTexto#123"));
        String hash = Senhas.gerarHash("SomenteTeste#123", Senhas.encoder());
        usuario.definirSenhaHash(hash);
        assertEquals(hash, usuario.senhaHash());
        assertFalse(usuario.toString().contains(hash));
    }
}

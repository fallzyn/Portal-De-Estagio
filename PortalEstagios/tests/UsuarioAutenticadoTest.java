import model.Perfil;
import model.Usuario;
import org.junit.jupiter.api.Test;
import repository.UsuarioRepository;
import security.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioAutenticadoTest {
    @Test void carregaIdPerfilEHashDoBanco() {
        var repositorio = mock(UsuarioRepository.class);
        var usuario = new Usuario(8, "Nome", "nome@exemplo.com", Perfil.EMPRESA, null, "Empresa");
        String hash = Senhas.encoder().encode("ApenasTeste#123");
        usuario.definirSenhaHash(hash);
        when(repositorio.findByEmail(usuario.email())).thenReturn(Optional.of(usuario));
        var principal = new PortalUserDetailsService(repositorio).loadUserByUsername(usuario.email());
        assertTrue(principal.isEnabled());
        assertEquals(hash, principal.getPassword());
        assertEquals("ROLE_EMPRESA", principal.getAuthorities().iterator().next().getAuthority());
        assertEquals(8, ((UsuarioAutenticado) principal).id());
    }
    @Test void contaSemHashFicaDesabilitada() {
        var principal = new UsuarioAutenticado(8, "Nome", "nome@exemplo.com", Perfil.ESTUDANTE, null);
        assertFalse(principal.isEnabled());
    }
    @Test void apagaCredenciaisSemPerderIdentidadeOuHabilitacao() {
        String hash = Senhas.encoder().encode("ApenasTeste#123");
        var principal = new UsuarioAutenticado(8, "Nome", "nome@exemplo.com", Perfil.ESTUDANTE, hash);
        principal.eraseCredentials();
        assertNull(principal.getPassword());
        assertTrue(principal.isEnabled());
        assertFalse(principal.toString().contains(hash));
    }
}

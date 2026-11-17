package service;

import model.Perfil;
import model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.UsuarioRepository;
import security.Senhas;

@Service
public class InicializacaoAdminService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    public InicializacaoAdminService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }
    @Transactional
    public void inicializar(String email, String senha) {
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Defina um e-mail válido para a inicialização administrativa.");
        }
        var existente = usuarios.findByEmail(email);
        if (existente.isPresent() && existente.get().perfil() != Perfil.ADMINISTRADOR) {
            throw new IllegalArgumentException("A inicialização não pode promover outra conta a administrador.");
        }
        // Reexecução não altera senhas previamente definidas.
        if (existente.isPresent() && existente.get().senhaHash() != null) return;
        String hash = Senhas.gerarHash(senha, encoder);
        var admin = existente.orElseGet(() -> new Usuario(0, "Administrador", email,
            Perfil.ADMINISTRADOR, null, null));
        admin.definirSenhaHash(hash);
        usuarios.saveAndFlush(admin);
    }
}

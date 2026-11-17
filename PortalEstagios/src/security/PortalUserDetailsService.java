package security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.UsuarioRepository;

@Service
public class PortalUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    public PortalUserDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        if (email == null) throw new UsernameNotFoundException("Credenciais inválidas.");
        var usuario = usuarios.findByEmail(email.trim())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
        return new UsuarioAutenticado(usuario.id(), usuario.nome(), usuario.email(),
            usuario.perfil(), usuario.senhaHash());
    }
}

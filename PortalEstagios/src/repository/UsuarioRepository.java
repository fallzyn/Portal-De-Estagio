package repository;

import java.util.Optional;
import model.Perfil;
import model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    long countByPerfil(Perfil perfil);
}
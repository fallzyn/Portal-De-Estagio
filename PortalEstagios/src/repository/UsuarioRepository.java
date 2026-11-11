package repository;

import java.util.Optional;
import java.util.List;
import model.Perfil;
import model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findAllByOrderByIdAsc();
    long countByPerfil(Perfil perfil);
}
package repository;

import java.util.List;
import model.Area;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<Area, Long> {
    List<Area> findAllByOrderByNomeAsc();
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, long id);
}
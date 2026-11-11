package repository;

import java.util.List;
import model.Candidatura;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidaturaRepository extends JpaRepository<Candidatura, Long> {
    boolean existsByEstudante_IdAndVaga_Id(long estudante, long vaga);

    @EntityGraph(attributePaths = {"estudante", "vaga", "vaga.empresa", "vaga.area"})
    List<Candidatura> findByEstudante_IdOrderByDataInscricaoDescIdDesc(long estudante);

    @EntityGraph(attributePaths = {"estudante", "vaga", "vaga.empresa", "vaga.area"})
    List<Candidatura> findByVaga_IdOrderByDataInscricaoAscIdAsc(long vaga);
}
package repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import model.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

public interface VagaRepository extends JpaRepository<Vaga, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vaga v where v.id = :id")
    Optional<Vaga> bloquearPorId(@Param("id") long id);

    boolean existsByArea_Id(long area);

    @EntityGraph(attributePaths = {"empresa", "area"})
    List<Vaga> findByEmpresa_IdOrderByIdAsc(long empresa);

    @EntityGraph(attributePaths = {"empresa", "area"})
    @Query("""
        select v from Vaga v where v.situacao = model.Situacao.ABERTA
        and (:area is null or v.area.id = :area)
        and (:modalidade is null or v.modalidade = :modalidade)
        order by v.id
        """)
    List<Vaga> listarAbertas(@Param("area") Long area,
                           @Param("modalidade") Modalidade modalidade);

    long countBySituacao(Situacao situacao);
}
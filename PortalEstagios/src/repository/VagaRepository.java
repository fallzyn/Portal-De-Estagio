package repository;

import java.util.List;
import model.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VagaRepository extends JpaRepository<Vaga, Long> {
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
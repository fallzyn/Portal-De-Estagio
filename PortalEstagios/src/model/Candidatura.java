package model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "candidaturas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"estudante_id", "vaga_id"})
})
public class Candidatura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudante_id", nullable = false)
    private Usuario estudante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    // A coluna existente é TIMESTAMP; LocalDate sozinho mapearia para DATE.
    @Column(name = "data_inscricao", nullable = false, updatable = false)
    private LocalDateTime dataInscricao;

    protected Candidatura() {
    }

    public Candidatura(Usuario estudante, Vaga vaga) {
        this.estudante = estudante;
        this.vaga = vaga;
        this.dataInscricao = LocalDateTime.now(ZoneOffset.UTC);
    }

    public long id() { return id == null ? 0 : id; }
    public long estudanteId() { return estudante.id(); }
    public Usuario estudante() { return estudante; }
    public long vagaId() { return vaga.id(); }
    public Vaga vaga() { return vaga; }
    public LocalDate dataInscricao() { return dataInscricao.toLocalDate(); }
    public LocalDateTime dataInscricaoCompleta() { return dataInscricao; }

    @Override
    public String toString() {
        return "Candidatura[id=" + id() + ", estudanteId=" + estudanteId()
            + ", vagaId=" + vagaId() + ", dataInscricao=" + dataInscricao() + "]";
    }
}
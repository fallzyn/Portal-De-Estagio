package model;
import java.time.LocalDateTime;
public record Candidatura(long id, long estudanteId, long vagaId, LocalDateTime dataInscricao) {
}

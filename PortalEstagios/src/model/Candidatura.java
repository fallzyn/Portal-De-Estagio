package model;

import java.time.LocalDate;

public record Candidatura(
    long id,
    long estudanteId,
    long vagaId,
    LocalDate dataInscricao
) {
}
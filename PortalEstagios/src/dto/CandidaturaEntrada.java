package dto;
import jakarta.validation.constraints.*;
public record CandidaturaEntrada(@NotNull @Positive Long vagaId) { }

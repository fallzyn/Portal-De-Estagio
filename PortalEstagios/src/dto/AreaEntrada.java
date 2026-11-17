package dto;
import jakarta.validation.constraints.*;
public record AreaEntrada(@NotBlank @Size(max = 100) String nome) { }

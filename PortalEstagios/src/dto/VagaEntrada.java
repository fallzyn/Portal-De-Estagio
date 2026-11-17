package dto;

import jakarta.validation.constraints.*;
import model.Modalidade;

public record VagaEntrada(
    @NotBlank @Size(max = 150) String titulo,
    @NotBlank String descricao,
    @NotNull @Positive Long areaId,
    @NotBlank @Size(max = 150) String localizacao,
    @NotNull Modalidade modalidade,
    @NotNull @Positive Integer cargaHoraria
) { }

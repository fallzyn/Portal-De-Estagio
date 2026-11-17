package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import model.Perfil;

public record CadastroEntrada(
    @NotBlank(message = "Informe o nome.") @Size(max = 150) String nome,
    @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") @Size(max = 200) String email,
    @NotNull(message = "Informe o perfil.") Perfil perfil,
    @Size(max = 150) String curso,
    @Size(max = 150) String nomeEmpresa,
    @NotBlank(message = "Informe a senha.") @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String senha
) {
    @Override public String toString() { return "CadastroEntrada[conteúdo protegido]"; }
}

package dto;

import model.Perfil;

public record UsuarioResposta(long id, String nome, String email, Perfil perfil) {
}

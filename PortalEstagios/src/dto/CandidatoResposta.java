package dto;
import model.Usuario;
// Contato do candidato disponível somente à empresa proprietária da vaga.
public record CandidatoResposta(long id, String nome, String email, String curso) {
    public static CandidatoResposta de(Usuario u) {
        return new CandidatoResposta(u.id(), u.nome(), u.email(), u.curso());
    }
}

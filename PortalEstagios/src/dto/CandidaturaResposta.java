package dto;
import java.time.LocalDateTime;
import model.Candidatura;
public record CandidaturaResposta(long id, LocalDateTime dataInscricao, VagaResposta vaga) {
    public static CandidaturaResposta de(Candidatura c) {
        return new CandidaturaResposta(c.id(), c.dataInscricaoCompleta(), VagaResposta.de(c.vaga()));
    }
}

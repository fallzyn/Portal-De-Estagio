package dto;

import model.Modalidade;
import model.Situacao;
import model.Vaga;

// Expõe os dados da vaga sem serializar as entidades de usuário e área.
public record VagaResposta(long id, long empresaId, String nomeEmpresa,
        String titulo, String descricao, long areaId, String nomeArea,
        String localizacao, Modalidade modalidade, int cargaHoraria, Situacao situacao) {
    public static VagaResposta de(Vaga vaga) {
        return new VagaResposta(vaga.id(), vaga.empresaId(), vaga.empresa().nomeEmpresa(),
            vaga.titulo(), vaga.descricao(), vaga.areaId(), vaga.area().nome(),
            vaga.localizacao(), vaga.modalidade(), vaga.cargaHoraria(), vaga.situacao());
    }
}

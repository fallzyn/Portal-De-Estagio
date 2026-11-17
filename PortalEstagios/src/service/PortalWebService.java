package service;

import dto.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Monta DTOs dentro da transação: relações LAZY não escapam para o controller.
@Service
@Transactional
public class PortalWebService {
    private final PortalService portal;
    public PortalWebService(PortalService portal) { this.portal = portal; }

    public VagaResposta publicar(long empresa, VagaEntrada entrada) {
        return VagaResposta.de(portal.publicar(empresa, entrada.titulo(), entrada.descricao(),
            entrada.areaId(), entrada.localizacao(), entrada.modalidade(), entrada.cargaHoraria()));
    }
    @Transactional(readOnly = true)
    public List<VagaResposta> vagas(long empresa) {
        return portal.vagasEmpresa(empresa).stream().map(VagaResposta::de).toList();
    }
    public void editar(long empresa, long vaga, VagaEntrada entrada) {
        portal.editarVaga(empresa, vaga, entrada.titulo(), entrada.descricao(), entrada.areaId(),
            entrada.localizacao(), entrada.modalidade(), entrada.cargaHoraria());
    }
    public void encerrar(long empresa, long vaga) { portal.encerrar(empresa, vaga); }
    @Transactional(readOnly = true)
    public List<CandidatoResposta> candidatos(long empresa, long vaga) {
        return portal.candidatos(empresa, vaga).stream().map(CandidatoResposta::de).toList();
    }
    public CandidaturaResposta candidatar(long estudante, long vaga) {
        return CandidaturaResposta.de(portal.candidatar(estudante, vaga));
    }
    @Transactional(readOnly = true)
    public List<CandidaturaResposta> minhas(long estudante) {
        return portal.minhas(estudante).stream().map(CandidaturaResposta::de).toList();
    }
}

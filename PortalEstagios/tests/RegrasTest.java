import java.util.List;
import java.util.Optional;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.*;
import service.PortalService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegrasTest {
    @Mock private UsuarioRepository usuarios;
    @Mock private AreaRepository areas;
    @Mock private VagaRepository vagas;
    @Mock private CandidaturaRepository candidaturas;
    private PortalService service;
    private final Usuario aluno = new Usuario(1, "Aluno", "a@a.com", Perfil.ESTUDANTE, "SI", null);
    private final Usuario empresa = new Usuario(2, "Empresa A", "b@b.com", Perfil.EMPRESA, null, "A");
    private final Usuario outra = new Usuario(3, "Empresa B", "c@c.com", Perfil.EMPRESA, null, "B");
    private final Usuario admin = new Usuario(4, "Admin", "admin@a.com", Perfil.ADMINISTRADOR, null, null);
    private final Area area = new Area(1, "Tecnologia");
    private final Vaga vaga = new Vaga(10, empresa, "Estágio", "Descrição", area, "SP", Modalidade.REMOTA, 30, Situacao.ABERTA);

    @BeforeEach
    void preparar() {
        service = new PortalService(usuarios, areas, vagas, candidaturas);
    }

    private void usuario(Usuario usuario) {
        when(usuarios.findById(usuario.id())).thenReturn(Optional.of(usuario));
    }

    private void bloquearVaga() {
        when(vagas.bloquearPorId(10)).thenReturn(Optional.of(vaga));
    }

    @Test void candidaturaDuplicada() {
        usuario(aluno);
        bloquearVaga();
        when(candidaturas.existsByEstudante_IdAndVaga_Id(1, 10)).thenReturn(false, true);
        service.candidatar(1, 10);
        assertThrows(IllegalArgumentException.class, () -> service.candidatar(1, 10));
        verify(candidaturas, times(1)).saveAndFlush(any(Candidatura.class));
    }

    @Test void vagaEncerrada() {
        usuario(empresa);
        usuario(aluno);
        bloquearVaga();
        service.encerrar(2, 10);
        assertThrows(IllegalArgumentException.class, () -> service.candidatar(1, 10));
        verify(candidaturas, never()).saveAndFlush(any());
    }

    @Test void vagaDeOutraEmpresa() {
        usuario(outra);
        bloquearVaga();
        when(vagas.findById(10L)).thenReturn(Optional.of(vaga));
        assertThrows(IllegalArgumentException.class, () -> service.editarVaga(3, 10, "Outro", "Descrição", 1, "SP", Modalidade.REMOTA, 20));
        assertThrows(IllegalArgumentException.class, () -> service.encerrar(3, 10));
        assertThrows(IllegalArgumentException.class, () -> service.candidatos(3, 10));
        verify(vagas, never()).saveAndFlush(any());
        verifyNoInteractions(candidaturas);
    }

    @Test void perfilNuloNaoPodeCadastrar() {
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar("Nome", "nome@exemplo.com", null, null, "Empresa"));
        verifyNoInteractions(usuarios);
    }

    @Test void cadastroPublicoNaoCriaAdministrador() {
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar("Nome", "nome@exemplo.com", Perfil.ADMINISTRADOR, null, null));
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test void somenteEmpresaPublica() {
        usuario(aluno);
        assertThrows(IllegalArgumentException.class, () -> service.publicar(1, "Estágio", "Descrição", 1, "SP", Modalidade.REMOTA, 30));
        verifyNoInteractions(vagas);
    }

    @Test void somenteEstudanteSeCandidata() {
        usuario(empresa);
        assertThrows(IllegalArgumentException.class, () -> service.candidatar(2, 10));
        verifyNoInteractions(candidaturas, vagas);
    }

    @Test void somenteAdministradorAlteraAreasEConsultaContagens() {
        usuario(aluno);
        usuario(empresa);
        assertThrows(IllegalArgumentException.class, () -> service.criarArea(1, "Design"));
        assertThrows(IllegalArgumentException.class, () -> service.editarArea(2, 1, "Design"));
        assertThrows(IllegalArgumentException.class, () -> service.excluirArea(1, 1));
        assertThrows(IllegalArgumentException.class, () -> service.contagens(2));
        verifyNoInteractions(areas, vagas, candidaturas);
    }

    @Test void vagaSemCamposObrigatoriosNaoPodeSerPublicada() {
        usuario(empresa);
        assertThrows(IllegalArgumentException.class, () -> service.publicar(2, " ", "Descrição", 1, "SP", Modalidade.REMOTA, 30));
        assertThrows(IllegalArgumentException.class, () -> service.publicar(2, "Estágio", " ", 1, "SP", Modalidade.REMOTA, 30));
        assertThrows(IllegalArgumentException.class, () -> service.publicar(2, "Estágio", "Descrição", 1, " ", Modalidade.REMOTA, 30));
        assertThrows(IllegalArgumentException.class, () -> service.publicar(2, "Estágio", "Descrição", 1, "SP", null, 30));
        assertThrows(IllegalArgumentException.class, () -> service.publicar(2, "Estágio", "Descrição", 1, "SP", Modalidade.REMOTA, 0));
        assertThrows(IllegalArgumentException.class, () -> service.publicar(2, "Estágio", "Descrição", 1, "SP", Modalidade.REMOTA, -1));
        verifyNoInteractions(vagas);
    }

    @Test void vagaEncerradaNaoPodeSerEditadaOuReaberta() {
        usuario(empresa);
        bloquearVaga();
        service.encerrar(2, 10);
        assertThrows(IllegalArgumentException.class, () -> service.editarVaga(2, 10, "Outro", "Descrição", 1, "SP", Modalidade.REMOTA, 20));
        assertThrows(IllegalArgumentException.class, () -> service.encerrar(2, 10));
        assertEquals(Situacao.ENCERRADA, vaga.situacao());
        verify(vagas, times(1)).saveAndFlush(vaga);
    }

    @Test void consultaPublicaNaoMostraVagaEncerrada() {
        vaga.encerrar();
        when(vagas.listarAbertas(null, null)).thenReturn(List.of(vaga));
        assertTrue(service.abertas(null, null).isEmpty());
    }

    @Test void cadastroValidoUsaRepositoryEPreservaDadosDePerfil() {
        service.cadastrar("Aluno", "novo@exemplo.com", Perfil.ESTUDANTE, "SI", "Ignorar");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).saveAndFlush(captor.capture());
        assertEquals(Perfil.ESTUDANTE, captor.getValue().perfil());
        assertEquals("SI", captor.getValue().curso());
        assertNull(captor.getValue().nomeEmpresa());
    }

    @Test void emailDuplicadoNaoGravaOutroUsuario() {
        when(usuarios.existsByEmail("a@a.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar("Aluno", "a@a.com", Perfil.ESTUDANTE, "SI", null));
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test void publicacaoValidaUsaAssociacoesDosRepositories() {
        usuario(empresa);
        when(areas.findById(1L)).thenReturn(Optional.of(area));
        service.publicar(2, "Estágio", "Descrição", 1, "SP", Modalidade.REMOTA, 30);
        ArgumentCaptor<Vaga> captor = ArgumentCaptor.forClass(Vaga.class);
        verify(vagas).saveAndFlush(captor.capture());
        assertSame(empresa, captor.getValue().empresa());
        assertSame(area, captor.getValue().area());
        assertEquals(Situacao.ABERTA, captor.getValue().situacao());
    }

    @Test void edicaoValidaPreservaProprietarioESituacao() {
        usuario(empresa);
        bloquearVaga();
        when(areas.findById(1L)).thenReturn(Optional.of(area));
        service.editarVaga(2, 10, "Editada", "Nova descrição", 1, "RJ", Modalidade.HIBRIDA, 20);
        assertSame(empresa, vaga.empresa());
        assertEquals(Situacao.ABERTA, vaga.situacao());
        assertEquals("Editada", vaga.titulo());
        assertEquals(20, vaga.cargaHoraria());
        verify(vagas).saveAndFlush(vaga);
    }

    @Test void areaInexistenteNaoPodeSerUsadaNaPublicacao() {
        usuario(empresa);
        assertThrows(IllegalArgumentException.class, () -> service.publicar(2, "Estágio", "Descrição", 999, "SP", Modalidade.REMOTA, 30));
        verify(vagas, never()).saveAndFlush(any());
    }

    @Test void minhasCandidaturasConsultaSomenteOEstudanteSolicitado() {
        usuario(aluno);
        Candidatura inscricao = new Candidatura(aluno, vaga);
        when(candidaturas.findByEstudante_IdOrderByDataInscricaoDescIdDesc(1)).thenReturn(List.of(inscricao));
        assertEquals(List.of(inscricao), service.minhas(1));
        verify(candidaturas).findByEstudante_IdOrderByDataInscricaoDescIdDesc(1);
        verifyNoMoreInteractions(candidaturas);
    }

    @Test void contagensAdministrativasUsamOsRepositories() {
        usuario(admin);
        when(usuarios.countByPerfil(Perfil.ESTUDANTE)).thenReturn(2L);
        when(usuarios.countByPerfil(Perfil.EMPRESA)).thenReturn(3L);
        when(vagas.countBySituacao(Situacao.ABERTA)).thenReturn(4L);
        when(vagas.countBySituacao(Situacao.ENCERRADA)).thenReturn(5L);
        when(candidaturas.count()).thenReturn(6L);
        assertEquals(new Contagens(2, 3, 4, 5, 6), service.contagens(4));
    }

    @Test void areaEmUsoNaoPodeSerExcluida() {
        usuario(admin);
        when(areas.findById(1L)).thenReturn(Optional.of(area));
        when(vagas.existsByArea_Id(1)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.excluirArea(4, 1));
        verify(areas, never()).delete(any());
    }
}

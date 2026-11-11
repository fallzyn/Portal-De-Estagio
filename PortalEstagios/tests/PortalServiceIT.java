import app.PortalApplication;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import repository.*;
import service.PortalService;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PortalApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class PortalServiceIT extends BancoPostgresTestSupport {
    @Autowired private PortalService portal;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private AreaRepository areas;
    @Autowired private VagaRepository vagas;
    @Autowired private CandidaturaRepository candidaturas;
    @Autowired private EntityManager entidades;
    @Autowired private PlatformTransactionManager transacoes;

    private Usuario admin;
    private Usuario empresa;
    private Usuario outraEmpresa;
    private Usuario aluno;
    private Area area;
    private Vaga vaga;

    @BeforeEach
    void prepararPeloServicoJpa() {
        String sufixo = UUID.randomUUID().toString();
        admin = usuarios.findByEmail("admin@exemplo.com").orElseThrow();
        empresa = portal.cadastrar("Empresa A", "empresa.a." + sufixo + "@exemplo.com", Perfil.EMPRESA, null, "A");
        outraEmpresa = portal.cadastrar("Empresa B", "empresa.b." + sufixo + "@exemplo.com", Perfil.EMPRESA, null, "B");
        aluno = portal.cadastrar("Aluno A", "aluno." + sufixo + "@exemplo.com", Perfil.ESTUDANTE, "SI", null);
        area = portal.criarArea(admin.id(), "Área serviço " + sufixo);
        vaga = portal.publicar(empresa.id(), "Vaga serviço", "Descrição", area.id(), "SP", Modalidade.REMOTA, 30);
    }

    @Test
    void cadastroEEmailUnicoPeloServico() {
        assertTrue(portal.usuarios().stream().anyMatch(u -> u.email().equals(aluno.email())));
        long antes = usuarios.count();
        assertThrows(IllegalArgumentException.class,
            () -> portal.cadastrar("Repetido", aluno.email(), Perfil.ESTUDANTE, "SI", null));
        assertEquals(antes, usuarios.count());
        assertEquals("A", usuarios.findById(empresa.id()).orElseThrow().nomeEmpresa());
    }

    @Test
    void publicacaoFiltrosECamposInvalidos() {
        assertEquals(1, portal.abertas(area.id(), Modalidade.REMOTA).size());
        assertTrue(portal.abertas(area.id(), Modalidade.PRESENCIAL).isEmpty());
        long antes = vagas.count();
        assertThrows(IllegalArgumentException.class,
            () -> portal.publicar(empresa.id(), " ", "Descrição", area.id(), "SP", Modalidade.REMOTA, 30));
        assertThrows(IllegalArgumentException.class,
            () -> portal.publicar(empresa.id(), "Inválida", "Descrição", area.id(), "SP", Modalidade.REMOTA, 0));
        assertEquals(antes, vagas.count());
    }

    @Test
    void candidaturaValidaEDuplicidadeBloqueada() {
        Candidatura inscricao = portal.candidatar(aluno.id(), vaga.id());
        assertTrue(inscricao.id() > 0);
        assertThrows(IllegalArgumentException.class, () -> portal.candidatar(aluno.id(), vaga.id()));
        assertEquals(1, portal.minhas(aluno.id()).size());
    }

    @Test
    void encerramentoBloqueiaNovasInscricoesEPreservaAnteriores() {
        portal.candidatar(aluno.id(), vaga.id());
        portal.encerrar(empresa.id(), vaga.id());
        Usuario outroAluno = portal.cadastrar("Outro", UUID.randomUUID() + "@exemplo.com", Perfil.ESTUDANTE, "SI", null);
        assertThrows(IllegalArgumentException.class, () -> portal.candidatar(outroAluno.id(), vaga.id()));
        assertTrue(portal.abertas(area.id(), null).isEmpty());
        assertEquals(1, portal.candidatos(empresa.id(), vaga.id()).size());
        assertEquals(1, portal.minhas(aluno.id()).size());
    }

    @Test
    void propriedadeBloqueiaAsTresOperacoesDeOutraEmpresa() {
        long empresaA = empresa.id(), empresaB = outraEmpresa.id(), id = vaga.id();
        assertThrows(IllegalArgumentException.class,
            () -> portal.editarVaga(empresaB, id, "Intrusão", "Descrição", area.id(), "RJ", Modalidade.HIBRIDA, 20));
        assertThrows(IllegalArgumentException.class, () -> portal.encerrar(empresaB, id));
        assertThrows(IllegalArgumentException.class, () -> portal.candidatos(empresaB, id));
        assertEquals("Vaga serviço", vaga.titulo());
        assertEquals(Situacao.ABERTA, vaga.situacao());
        portal.editarVaga(empresaA, id, "Editada", "Nova descrição", area.id(), "RJ", Modalidade.HIBRIDA, 20);
        entidades.clear();
        Vaga persistida = vagas.findById(id).orElseThrow();
        assertEquals("Editada", persistida.titulo());
        assertEquals(empresaA, persistida.empresaId());
        assertEquals(Situacao.ABERTA, persistida.situacao());
    }

    @Test
    void consultasSeparamInscricoesECandidatos() {
        Usuario outroAluno = portal.cadastrar("Outro", UUID.randomUUID() + "@exemplo.com", Perfil.ESTUDANTE, "SI", null);
        Vaga outraVaga = portal.publicar(outraEmpresa.id(), "Outra vaga", "Descrição", area.id(), "RJ", Modalidade.HIBRIDA, 20);
        portal.candidatar(aluno.id(), vaga.id());
        portal.candidatar(outroAluno.id(), outraVaga.id());
        assertEquals(vaga.id(), portal.minhas(aluno.id()).get(0).vagaId());
        assertEquals(outraVaga.id(), portal.minhas(outroAluno.id()).get(0).vagaId());
        assertEquals(1, portal.minhas(aluno.id()).size());
        assertEquals(1, portal.minhas(outroAluno.id()).size());
        assertEquals(aluno.id(), portal.candidatos(empresa.id(), vaga.id()).get(0).id());
        assertEquals(outroAluno.id(), portal.candidatos(outraEmpresa.id(), outraVaga.id()).get(0).id());
    }

    @Test
    void areaEmUsoEAreaLivreSeguemRegras() {
        assertThrows(IllegalArgumentException.class, () -> portal.excluirArea(admin.id(), area.id()));
        assertTrue(areas.findById(area.id()).isPresent());
        Area livre = portal.criarArea(admin.id(), "Livre " + UUID.randomUUID());
        long id = livre.id();
        String nome = "Editada " + UUID.randomUUID();
        portal.editarArea(admin.id(), id, nome);
        assertEquals(nome, areas.findById(id).orElseThrow().nome());
        portal.excluirArea(admin.id(), id);
        assertTrue(areas.findById(id).isEmpty());
    }

    @Test
    void contagensConferemComOperacoesRealizadas() {
        portal.candidatar(aluno.id(), vaga.id());
        portal.encerrar(empresa.id(), vaga.id());
        assertEquals(new Contagens(2, 3, 1, 1, 1), portal.contagens(admin.id()));
    }

    @Test
    void perfisIncorretosNaoGravam() {
        long antes = vagas.count();
        assertThrows(IllegalArgumentException.class,
            () -> portal.publicar(aluno.id(), "Não permitido", "Descrição", area.id(), "SP", Modalidade.REMOTA, 30));
        assertThrows(IllegalArgumentException.class, () -> portal.candidatar(empresa.id(), vaga.id()));
        assertThrows(IllegalArgumentException.class, () -> portal.criarArea(aluno.id(), "Proibida"));
        assertThrows(IllegalArgumentException.class, () -> portal.contagens(empresa.id()));
        assertEquals(antes, vagas.count());
    }

    @Test
    void falhaDesfazGravacoesDaMesmaTransacao() {
        String email = "rollback." + UUID.randomUUID() + "@exemplo.com";
        TransactionTemplate trabalho = new TransactionTemplate(transacoes);
        trabalho.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        assertThrows(IllegalArgumentException.class, () -> trabalho.execute(status -> {
            Usuario novo = portal.cadastrar("Temporário", email, Perfil.ESTUDANTE, "SI", null);
            // Uma segunda operação inválida deve desfazer também o cadastro acima.
            return portal.publicar(novo.id(), "Proibida", "Descrição", 1, "SP", Modalidade.REMOTA, 30);
        }));
        assertFalse(usuarios.existsByEmail(email));
    }
}

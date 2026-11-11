import app.PortalApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import repository.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PortalApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class JpaRepositoryIT {
    @Autowired private AreaRepository areas;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private VagaRepository vagas;
    @Autowired private CandidaturaRepository candidaturas;
    @Autowired private EntityManager entidades;
    @Autowired private ObjectMapper json;
    @LocalServerPort private int porta;

    private Area area;
    private Usuario empresa;
    private Usuario estudante;
    private Vaga vaga;

    @DynamicPropertySource
    static void bancoExclusivoDeTestes(DynamicPropertyRegistry propriedades) {
        String url = System.getenv("TEST_DB_URL");
        if (url == null || !url.startsWith("jdbc:postgresql://")) {
            throw new IllegalStateException("Configure TEST_DB_URL para um PostgreSQL local exclusivo de testes.");
        }
        URI endereco = URI.create(url.substring("jdbc:".length()));
        if (!("localhost".equals(endereco.getHost()) || "127.0.0.1".equals(endereco.getHost()))
                || !"/portal_estagios_test".equals(endereco.getPath())) {
            throw new IllegalStateException("Os testes exigem PostgreSQL local e banco portal_estagios_test. Não use Supabase.");
        }
        String usuario = System.getenv("TEST_DB_USER");
        String senha = System.getenv("TEST_DB_PASSWORD");
        if (usuario == null || senha == null) {
            throw new IllegalStateException("Configure TEST_DB_USER e TEST_DB_PASSWORD, sem reutilizar credenciais de produção.");
        }
        propriedades.add("spring.datasource.url", () -> url);
        propriedades.add("spring.datasource.username", () -> usuario);
        propriedades.add("spring.datasource.password", () -> senha);
    }

    @BeforeEach
    void prepararDadosDaTransacao() {
        String sufixo = UUID.randomUUID().toString();
        area = areas.saveAndFlush(new Area(0, "Área JPA " + sufixo));
        empresa = usuarios.saveAndFlush(new Usuario(0, "Empresa JPA", "empresa." + sufixo + "@exemplo.com",
            Perfil.EMPRESA, null, "Empresa JPA"));
        estudante = usuarios.saveAndFlush(new Usuario(0, "Estudante JPA", "aluno." + sufixo + "@exemplo.com",
            Perfil.ESTUDANTE, "SI", null));
        vaga = vagas.saveAndFlush(new Vaga(0, empresa, "Estágio JPA", "Descrição", area,
            "SP", Modalidade.REMOTA, 30, Situacao.ABERTA));
    }

    @Test
    void leDadosDoEsquemaOriginalSemRecriarTabelas() {
        assertTrue(usuarios.findByEmail("admin@exemplo.com").isPresent());
        assertTrue(areas.findAllByOrderByNomeAsc().stream().anyMatch(a -> a.nome().equals("Tecnologia")));
        assertTrue(usuarios.countByPerfil(Perfil.EMPRESA) >= 2);
        assertTrue(vagas.countBySituacao(Situacao.ABERTA) >= 2);
    }

    @Test
    void filtraVagasAbertasPorAreaEModalidade() {
        Area outra = areas.saveAndFlush(new Area(0, "Outra " + UUID.randomUUID()));
        vagas.saveAndFlush(new Vaga(0, empresa, "Presencial", "Descrição", outra,
            "RJ", Modalidade.PRESENCIAL, 20, Situacao.ABERTA));
        assertEquals(1, vagas.listarAbertas(area.id(), Modalidade.REMOTA).size());
        assertTrue(vagas.listarAbertas(area.id(), Modalidade.PRESENCIAL).isEmpty());
        assertFalse(vagas.listarAbertas(null, null).isEmpty());
        assertEquals(2, vagas.findByEmpresa_IdOrderByIdAsc(empresa.id()).size());
    }

    @Test
    void editaEExcluiAreaLivre() {
        Area livre = areas.saveAndFlush(new Area(0, "Livre " + UUID.randomUUID()));
        long id = livre.id();
        livre.editar("Editada " + UUID.randomUUID());
        String nome = livre.nome();
        areas.saveAndFlush(livre);
        entidades.clear();
        assertEquals(nome, areas.findById(id).orElseThrow().nome());
        areas.deleteById(id);
        areas.flush();
        assertTrue(areas.findById(id).isEmpty());
    }

    @Test
    void rejeitaEmailDuplicadoNoPostgreSQL() {
        assertThrows(DataIntegrityViolationException.class, () -> usuarios.saveAndFlush(
            new Usuario(0, "Outro", estudante.email(), Perfil.ESTUDANTE, "SI", null)));
    }

    @Test
    void gravaCandidaturaComTimestampEFiltraEstudante() {
        LocalDateTime antes = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);
        Candidatura inscricao = candidaturas.saveAndFlush(new Candidatura(estudante, vaga));
        long id = inscricao.id();
        long aluno = estudante.id();
        long idVaga = vaga.id();
        entidades.clear();
        Candidatura recarregada = candidaturas.findById(id).orElseThrow();
        assertTrue(recarregada.dataInscricaoCompleta().isAfter(antes));
        assertEquals(aluno, recarregada.estudanteId());
        assertEquals(idVaga, recarregada.vagaId());
        assertTrue(candidaturas.existsByEstudante_IdAndVaga_Id(aluno, idVaga));
        assertEquals(1, candidaturas.findByEstudante_IdOrderByDataInscricaoDescIdDesc(aluno).size());
        assertTrue(candidaturas.findByEstudante_IdOrderByDataInscricaoDescIdDesc(empresa.id()).isEmpty());
    }

    @Test
    void rejeitaCandidaturaDuplicadaNoPostgreSQL() {
        candidaturas.saveAndFlush(new Candidatura(estudante, vaga));
        assertThrows(DataIntegrityViolationException.class,
            () -> candidaturas.saveAndFlush(new Candidatura(estudante, vaga)));
    }

    @Test
    void impedeExclusaoDeAreaEmUso() {
        long id = area.id();
        // Limpa os objetos gerenciados para exercitar a FK do PostgreSQL,
        // em vez da verificação prévia de referências do Hibernate na sessão.
        entidades.clear();
        assertThrows(DataIntegrityViolationException.class, () -> {
            areas.deleteById(id);
            areas.flush();
        });
    }

    @Test
    void encerramentoPreservaInscricoesENaoApareceNaConsultaPublica() {
        candidaturas.saveAndFlush(new Candidatura(estudante, vaga));
        long idVaga = vaga.id();
        long idArea = area.id();
        vaga.encerrar();
        vagas.saveAndFlush(vaga);
        entidades.clear();
        assertEquals(Situacao.ENCERRADA, vagas.findById(idVaga).orElseThrow().situacao());
        assertTrue(vagas.listarAbertas(idArea, null).isEmpty());
        assertEquals(1, candidaturas.findByVaga_IdOrderByDataInscricaoAscIdAsc(idVaga).size());
    }

    @Test
    void consultaAreasPelaApiUsandoJpaReal() throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:" + porta + "/api/areas"))
            .timeout(Duration.ofSeconds(5)).GET().build();
        HttpResponse<String> resposta = HttpClient.newHttpClient()
            .send(pedido, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resposta.statusCode());
        JsonNode corpo = json.readTree(resposta.body());
        // Os dados da transação deste teste não são visíveis para a requisição HTTP.
        assertEquals(3, corpo.size());
        assertEquals("Administração", corpo.get(0).path("nome").asText());
        assertEquals("Design", corpo.get(1).path("nome").asText());
        assertEquals("Tecnologia", corpo.get(2).path("nome").asText());
        for (JsonNode item : corpo) {
            assertEquals(2, item.size());
            assertTrue(item.path("id").asLong() > 0);
        }
    }
}
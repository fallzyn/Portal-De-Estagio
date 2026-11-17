import app.PortalApplication;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import repository.*;
import static org.junit.jupiter.api.Assertions.*;

// HTTP real: a serialização acontece após o encerramento da transação do serviço.
@SpringBootTest(classes = PortalApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VagasIT extends BancoPostgresTestSupport {
    @Autowired private TestRestTemplate http;
    @Autowired private PlatformTransactionManager transacoes;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private AreaRepository areas;
    @Autowired private VagaRepository vagas;

    @Test void consultaPublicaFiltraNoPostgresSemExporContaNemCarregarRelacionamentosForaDaTransacao() {
        var trabalho = new TransactionTemplate(transacoes);
        long[] ids = trabalho.execute(status -> {
            String sufixo = UUID.randomUUID().toString();
            var empresa = usuarios.saveAndFlush(new Usuario(0, "Contato privado", sufixo + "@exemplo.com", Perfil.EMPRESA, null, "Empresa HTTP"));
            var area = areas.saveAndFlush(new Area(0, "Área HTTP " + sufixo));
            var outra = areas.saveAndFlush(new Area(0, "Outra HTTP " + sufixo));
            var remota = vagas.saveAndFlush(new Vaga(0, empresa, "Remota HTTP", "Descrição", area, "SP", Modalidade.REMOTA, 30, Situacao.ABERTA));
            var presencial = vagas.saveAndFlush(new Vaga(0, empresa, "Presencial HTTP", "Descrição", area, "SP", Modalidade.PRESENCIAL, 20, Situacao.ABERTA));
            var encerrada = vagas.saveAndFlush(new Vaga(0, empresa, "Encerrada HTTP", "Descrição", area, "SP", Modalidade.REMOTA, 30, Situacao.ENCERRADA));
            return new long[]{empresa.id(), area.id(), outra.id(), remota.id(), presencial.id(), encerrada.id()};
        });
        try {
            var resposta = http.getForEntity("/api/vagas?area=" + ids[1], JsonNode.class);
            assertEquals(200, resposta.getStatusCode().value());
            var corpo = resposta.getBody();
            assertNotNull(corpo);
            assertEquals(2, corpo.size());
            assertEquals(ids[3], corpo.get(0).path("id").asLong());
            assertEquals("Empresa HTTP", corpo.get(0).path("nomeEmpresa").asText());
            assertFalse(corpo.toString().contains("@exemplo.com"));
            assertFalse(corpo.toString().contains("senha"));
            assertFalse(corpo.toString().contains("Contato privado"));
            assertFalse(corpo.toString().contains("Encerrada HTTP"));
            var combinada = http.getForEntity("/api/vagas?area=" + ids[1] + "&modalidade=REMOTA", JsonNode.class);
            assertEquals(200, combinada.getStatusCode().value());
            assertEquals(1, combinada.getBody().size());
            assertEquals(ids[3], combinada.getBody().get(0).path("id").asLong());
            var somenteModalidade = http.getForEntity("/api/vagas?modalidade=PRESENCIAL", JsonNode.class);
            assertEquals(200, somenteModalidade.getStatusCode().value());
            assertTrue(somenteModalidade.getBody().size() > 0);
            somenteModalidade.getBody().forEach(v -> assertEquals("PRESENCIAL", v.path("modalidade").asText()));
            var todas = http.getForEntity("/api/vagas", JsonNode.class);
            assertEquals(200, todas.getStatusCode().value());
            todas.getBody().forEach(v -> assertEquals("ABERTA", v.path("situacao").asText()));
            var vazia = http.getForEntity("/api/vagas?area=" + ids[2], JsonNode.class);
            assertEquals(200, vazia.getStatusCode().value());
            assertEquals(0, vazia.getBody().size());
            assertEquals(400, http.getForEntity("/api/vagas?modalidade=ONLINE", JsonNode.class).getStatusCode().value());
            assertEquals(400, http.getForEntity("/api/vagas?area=abc", JsonNode.class).getStatusCode().value());
            assertEquals(401, http.getForEntity("/api/admin/contagens", String.class).getStatusCode().value());
        } finally {
            trabalho.executeWithoutResult(status -> {
                vagas.deleteAllById(List.of(ids[3], ids[4], ids[5]));
                vagas.flush();
                areas.deleteAllById(List.of(ids[1], ids[2]));
                areas.flush();
                usuarios.deleteById(ids[0]);
            });
        }
    }
}

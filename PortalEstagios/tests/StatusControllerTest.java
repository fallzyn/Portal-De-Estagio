import app.PortalApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PortalApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StatusControllerTest {
    @LocalServerPort
    private int porta;

    @Autowired
    private ObjectMapper json;

    @Test
    void servidorRespondeStatusPorHttpSemBancoDeDados() throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:" + porta + "/api/status"))
            .GET().build();
        HttpResponse<String> resposta = HttpClient.newHttpClient()
            .send(pedido, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resposta.statusCode());
        assertTrue(resposta.headers().firstValue("Content-Type").orElse("")
            .startsWith("application/json"));
        JsonNode corpo = json.readTree(resposta.body());
        assertEquals("portal-estagios", corpo.path("aplicacao").asText());
        assertEquals("ok", corpo.path("status").asText());
    }
}

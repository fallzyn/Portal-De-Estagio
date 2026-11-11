import java.net.URI;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

abstract class BancoPostgresTestSupport {
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
}
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private String executar(String entrada) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classes = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        // O processo filho deve emitir os mesmos bytes UTF-8 que o teste decodifica,
        // inclusive no Windows, onde stdout pode usar a página de código nativa.
        ProcessBuilder comando = new ProcessBuilder(java,
            "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
            "-cp", classes, "app.Main")
            .redirectErrorStream(true);
        // Estes cenários devem encerrar ou rejeitar a entrada antes de acessar o banco.
        comando.environment().remove("DB_PASSWORD");
        Process processo = comando.start();
        try {
            try (var teclado = processo.getOutputStream()) {
                teclado.write(entrada.getBytes(StandardCharsets.UTF_8));
            }
            assertTrue(processo.waitFor(10, TimeUnit.SECONDS), "O menu não encerrou.");
            String saida = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, processo.exitValue(), saida);
            assertFalse(saida.contains("Exception"), saida);
            assertFalse(saida.contains("Falha no banco:"), saida);
            return saida;
        } finally {
            processo.destroyForcibly();
        }
    }

    @Test void fimDaEntradaNoMenuEncerraSemErro() throws Exception {
        String saida = executar("");
        assertTrue(saida.contains("Entrada encerrada."), saida);
    }

    @Test void fimDaEntradaDuranteCadastroNaoGravaDados() throws Exception {
        String saida = executar("1\nNome incompleto\n");
        assertTrue(saida.contains("Entrada encerrada."), saida);
    }

    @Test void fimDaEntradaAposNumeroInvalidoEncerraSemErro() throws Exception {
        String saida = executar("4\nabc\n");
        assertTrue(saida.contains("Número inválido."), saida);
        assertTrue(saida.contains("Entrada encerrada."), saida);
    }

    @Test void modalidadeInvalidaNaoEncerraOMenu() throws Exception {
        String saida = executar("4\n\nqualquer\n0\n");
        assertTrue(saida.contains("Modalidade inválida. Use PRESENCIAL, REMOTA ou HIBRIDA."), saida);
        assertFalse(saida.contains("No enum constant"), saida);
    }

    @Test void sairEncerraNormalmente() throws Exception {
        assertFalse(executar("0\n").contains("Entrada encerrada."));
    }
}

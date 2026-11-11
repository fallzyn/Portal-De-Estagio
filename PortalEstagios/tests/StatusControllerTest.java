import controller.StatusController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatusController.class)
@ContextConfiguration(classes = StatusController.class)
class StatusControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void rotaRespondeStatusSemBancoDeDados() throws Exception {
        mvc.perform(get("/api/status"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.aplicacao").value("portal-estagios"))
            .andExpect(jsonPath("$.status").value("ok"));
    }
}
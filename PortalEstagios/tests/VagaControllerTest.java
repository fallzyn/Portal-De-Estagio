import config.SecurityConfig;
import config.SenhaConfig;
import controller.ApiErros;
import controller.VagaController;
import model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import service.PortalService;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@ContextConfiguration(classes = {VagaController.class, ApiErros.class})
@Import({SecurityConfig.class, SenhaConfig.class})
class VagaControllerTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private PortalService portal;
    @MockitoBean private UserDetailsService usuarios;

    @Test void consultaPublicaTemDadosDaVagaSemCredenciaisDaEmpresa() throws Exception {
        var empresa = new Usuario(3, "Responsável privado", "privado@exemplo.com", Perfil.EMPRESA, null, "Empresa Exemplo");
        var vaga = new Vaga(8, empresa, "Estágio", "Desenvolvimento", new Area(1, "Tecnologia"),
            "SP", Modalidade.REMOTA, 30, Situacao.ABERTA);
        when(portal.abertas(null, null)).thenReturn(List.of(vaga));
        mvc.perform(get("/api/vagas")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(8))
            .andExpect(jsonPath("$[0].nomeEmpresa").value("Empresa Exemplo"))
            .andExpect(jsonPath("$[0].nomeArea").value("Tecnologia"))
            .andExpect(jsonPath("$[0].situacao").value("ABERTA"))
            .andExpect(jsonPath("$[0].email").doesNotExist())
            .andExpect(jsonPath("$[0].senhaHash").doesNotExist())
            .andExpect(jsonPath("$[0].empresa").doesNotExist());
    }

    @ParameterizedTest
    @CsvSource({"1,,1,", ",REMOTA,,REMOTA", "1,HIBRIDA,1,HIBRIDA", ",PRESENCIAL,,PRESENCIAL"})
    void filtrosSaoEncaminhadosAoServico(String area, String modalidade, Long idEsperado, Modalidade modalidadeEsperada) throws Exception {
        when(portal.abertas(idEsperado, modalidadeEsperada)).thenReturn(List.of());
        var pedido = get("/api/vagas");
        if (area != null) pedido.param("area", area);
        if (modalidade != null) pedido.param("modalidade", modalidade);
        mvc.perform(pedido).andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(portal).abertas(idEsperado, modalidadeEsperada);
    }

    @ParameterizedTest
    @CsvSource({"area,0", "area,-1", "area,abc", "modalidade,ONLINE", "modalidade,remota"})
    void filtroInvalidoRetorna400SemConsultarBanco(String nome, String valor) throws Exception {
        mvc.perform(get("/api/vagas").param(nome, valor)).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensagem").isString());
        verifyNoInteractions(portal);
    }

    @Test void consultaPublicaNaoLiberaGravacao() throws Exception {
        mvc.perform(post("/api/vagas").contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(portal);
    }
}

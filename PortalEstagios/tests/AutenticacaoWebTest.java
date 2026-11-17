import com.fasterxml.jackson.databind.ObjectMapper;
import config.SecurityConfig;
import config.SenhaConfig;
import controller.*;
import dto.UsuarioResposta;
import model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.*;
import security.*;
import service.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(properties = "spring.jackson.deserialization.fail-on-unknown-properties=true",
    excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@ContextConfiguration(classes = {AuthController.class, StatusController.class,
    AdminController.class, ContaController.class, ApiErros.class})
@Import({SecurityConfig.class, SenhaConfig.class})
class AutenticacaoWebTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @MockitoBean private PortalUserDetailsService usuarios;
    @MockitoBean private CadastroService cadastros;
    @MockitoBean private PortalService portal;
    private static final String SENHA = "ApenasTeste#123";
    private static final String HASH = Senhas.encoder().encode(SENHA);
    private static final String EMAIL = "aluno@exemplo.com";
    private static final String CADASTRO = """
        {"nome":"Aluno","email":"aluno@exemplo.com","perfil":"ESTUDANTE","curso":"SI","senha":"ApenasTeste#123"}
        """;

    private record Token(MockHttpSession sessao, String cabecalho, String valor) { }
    private Token token(MockHttpSession sessao) throws Exception {
        var pedido = get("/api/auth/csrf");
        if (sessao != null) pedido.session(sessao);
        var resultado = mvc.perform(pedido).andExpect(status().isOk()).andReturn();
        var corpo = json.readTree(resultado.getResponse().getContentAsString());
        return new Token((MockHttpSession) resultado.getRequest().getSession(false),
            corpo.path("headerName").asText(), corpo.path("token").asText());
    }
    private void contaComSenha() {
        when(usuarios.loadUserByUsername(EMAIL)).thenAnswer(chamada ->
            new UsuarioAutenticado(7, "Aluno", EMAIL, Perfil.ESTUDANTE, HASH));
    }
    private MvcResult entrar(Token token, String senha) throws Exception {
        return mvc.perform(post("/api/auth/login").session(token.sessao())
            .header(token.cabecalho(), token.valor()).accept("application/json")
            .param("username", EMAIL).param("password", senha)).andReturn();
    }

    @Test void statusPublicoEContaProtegida() throws Exception {
        mvc.perform(get("/api/status")).andExpect(status().isOk());
        mvc.perform(get("/api/auth/eu").accept("application/json")).andExpect(status().isUnauthorized());
        mvc.perform(get("/")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/login")).andExpect(redirectedUrl("/login.html"));
    }

    @Test void loginExigeCsrf() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", EMAIL).param("password", SENHA))
            .andExpect(status().isForbidden());
        verifyNoInteractions(usuarios);
    }

    @Test void cadastroExigeCsrf() throws Exception {
        mvc.perform(post("/api/auth/cadastro").contentType("application/json").content(CADASTRO))
            .andExpect(status().isForbidden());
        verifyNoInteractions(cadastros);
    }

    @Test void cadastroValidoRetornaSomenteDto() throws Exception {
        var token = token(null);
        when(cadastros.cadastrar(any())).thenReturn(new UsuarioResposta(7, "Aluno", EMAIL, Perfil.ESTUDANTE));
        mvc.perform(post("/api/auth/cadastro").session(token.sessao()).header(token.cabecalho(), token.valor())
            .contentType("application/json").content(CADASTRO))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.senha").doesNotExist()).andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test void cadastroNaoAceitaCamposExtras() throws Exception {
        var token = token(null);
        mvc.perform(post("/api/auth/cadastro").session(token.sessao()).header(token.cabecalho(), token.valor())
            .contentType("application/json").content(CADASTRO.replace("\"nome\"", "\"id\":99,\"nome\"")))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(cadastros);
    }

    @Test void senhaIncorretaRetornaErroGenerico() throws Exception {
        contaComSenha();
        var resultado = entrar(token(null), "SenhaErrada#123");
        assertEquals(401, resultado.getResponse().getStatus());
        assertEquals("E-mail ou senha inválidos.", json.readTree(resultado.getResponse().getContentAsString()).path("mensagem").asText());
    }

    @Test void contaSemHashNaoEntraNemRecebeSenhaPadrao() throws Exception {
        when(usuarios.loadUserByUsername(EMAIL)).thenReturn(
            new UsuarioAutenticado(7, "Aluno", EMAIL, Perfil.ESTUDANTE, null));
        assertEquals(401, entrar(token(null), SENHA).getResponse().getStatus());
    }

    @Test void loginCriaSessaoRotacionaIdERetornaIdentidadeSemHash() throws Exception {
        contaComSenha();
        Token antes = token(null);
        String idAntigo = antes.sessao().getId();
        var resultado = entrar(antes, SENHA);
        assertEquals(200, resultado.getResponse().getStatus());
        var sessao = (MockHttpSession) resultado.getRequest().getSession(false);
        assertNotEquals(idAntigo, sessao.getId());
        mvc.perform(get("/api/auth/eu").session(sessao))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.email").value(EMAIL)).andExpect(jsonPath("$.perfil").value("ESTUDANTE"))
            .andExpect(jsonPath("$.senhaHash").doesNotExist()).andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test void logoutExigeTokenAtualEInvalidaSessao() throws Exception {
        contaComSenha();
        Token antes = token(null);
        var resultado = entrar(antes, SENHA);
        var sessao = (MockHttpSession) resultado.getRequest().getSession(false);
        mvc.perform(post("/logout").session(sessao).header(antes.cabecalho(), antes.valor()))
            .andExpect(status().isForbidden());
        Token novo = token(sessao);
        mvc.perform(post("/logout").session(sessao).header(novo.cabecalho(), novo.valor()))
            .andExpect(status().is3xxRedirection());
        assertTrue(sessao.isInvalid());
        mvc.perform(get("/api/auth/eu")).andExpect(status().isUnauthorized());
    }

    @Test void estudanteNaoAcessaContagensAdministrativas() throws Exception {
        var aluno = new UsuarioAutenticado(7, "Aluno", EMAIL, Perfil.ESTUDANTE, HASH);
        mvc.perform(get("/api/admin/contagens").with(user(aluno)))
            .andExpect(status().isForbidden());
        verifyNoInteractions(portal);
    }

    @Test void administradorUsaIdDaSessaoEIgnoraIdInformadoPeloCliente() throws Exception {
        var admin = new UsuarioAutenticado(42, "Admin", "admin@exemplo.com", Perfil.ADMINISTRADOR, HASH);
        when(portal.contagens(42)).thenReturn(new Contagens(1, 2, 3, 4, 5));
        mvc.perform(get("/api/admin/contagens?admin=999").with(user(admin)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estudantes").value(1));
        verify(portal).contagens(42);
    }
}

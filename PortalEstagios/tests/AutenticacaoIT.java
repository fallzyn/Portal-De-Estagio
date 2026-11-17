import app.PortalApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;
import repository.UsuarioRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PortalApplication.class)
@AutoConfigureMockMvc
@Transactional
class AutenticacaoIT extends BancoPostgresTestSupport {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PasswordEncoder encoder;
    private static final String SENHA = "SomenteTeste#123";
    private record Token(MockHttpSession sessao, String cabecalho, String valor) { }
    private Token token(MockHttpSession sessao) throws Exception {
        var pedido = get("/api/auth/csrf");
        if (sessao != null) pedido.session(sessao);
        var r = mvc.perform(pedido).andExpect(status().isOk()).andReturn();
        var corpo = json.readTree(r.getResponse().getContentAsString());
        return new Token((MockHttpSession) r.getRequest().getSession(false),
            corpo.path("headerName").asText(), corpo.path("token").asText());
    }
    private Map<String, Object> dados(String perfil) {
        var dados = new HashMap<String, Object>();
        dados.put("nome", "Nome de teste");
        dados.put("email", UUID.randomUUID() + "@exemplo.com");
        dados.put("perfil", perfil);
        dados.put("curso", "SI");
        dados.put("nomeEmpresa", "Empresa teste");
        dados.put("senha", SENHA);
        return dados;
    }
    private ResultActions cadastrar(Map<String, Object> dados) throws Exception {
        Token t = token(null);
        return mvc.perform(post("/api/auth/cadastro").session(t.sessao()).header(t.cabecalho(), t.valor())
            .contentType("application/json").content(json.writeValueAsString(dados)));
    }
    private MvcResult entrar(String email, String senha, Token t) throws Exception {
        return mvc.perform(post("/api/auth/login").session(t.sessao()).header(t.cabecalho(), t.valor())
            .accept("application/json").param("username", email).param("password", senha)).andReturn();
    }

    @Test void cadastroEstudanteGravaHashEPermiteLoginReal() throws Exception {
        var dados = dados("ESTUDANTE");
        var resposta = cadastrar(dados).andExpect(status().isCreated())
            .andExpect(jsonPath("$.senha").doesNotExist()).andExpect(jsonPath("$.senhaHash").doesNotExist()).andReturn();
        String email = dados.get("email").toString();
        var usuario = usuarios.findByEmail(email).orElseThrow();
        assertNotEquals(SENHA, usuario.senhaHash());
        assertTrue(encoder.matches(SENHA, usuario.senhaHash()));
        assertNull(usuario.nomeEmpresa());
        assertFalse(resposta.getResponse().getContentAsString().contains(usuario.senhaHash()));
        var login = entrar(email, SENHA, token(null));
        assertEquals(200, login.getResponse().getStatus());
        mvc.perform(get("/api/auth/eu").session((MockHttpSession) login.getRequest().getSession(false)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(usuario.id()))
            .andExpect(jsonPath("$.perfil").value("ESTUDANTE"));
    }

    @Test void cadastroEmpresaPreservaPerfilSemCurso() throws Exception {
        var dados = dados("EMPRESA");
        cadastrar(dados).andExpect(status().isCreated()).andExpect(jsonPath("$.perfil").value("EMPRESA"));
        var usuario = usuarios.findByEmail(dados.get("email").toString()).orElseThrow();
        assertNull(usuario.curso());
        assertEquals("Empresa teste", usuario.nomeEmpresa());
        assertTrue(encoder.matches(SENHA, usuario.senhaHash()));
    }

    @Test void cadastroPublicoNaoCriaAdministrador() throws Exception {
        var dados = dados("ADMINISTRADOR");
        cadastrar(dados).andExpect(status().isBadRequest());
        assertTrue(usuarios.findByEmail(dados.get("email").toString()).isEmpty());
    }

    @Test void senhaInvalidaNaoCriaCadastroIncompleto() throws Exception {
        var dados = dados("ESTUDANTE");
        dados.put("senha", "curta");
        cadastrar(dados).andExpect(status().isBadRequest());
        assertTrue(usuarios.findByEmail(dados.get("email").toString()).isEmpty());
        dados.put("senha", "á".repeat(37));
        cadastrar(dados).andExpect(status().isBadRequest());
        assertTrue(usuarios.findByEmail(dados.get("email").toString()).isEmpty());
    }

    @Test void emailDuplicadoRetornaConflito() throws Exception {
        var dados = dados("ESTUDANTE");
        cadastrar(dados).andExpect(status().isCreated());
        long quantidade = usuarios.count();
        cadastrar(dados).andExpect(status().isConflict());
        assertEquals(quantidade, usuarios.count());
    }

    @Test void contaLegadaSemHashNaoPodeEntrar() throws Exception {
        assertNull(usuarios.findByEmail("ana@exemplo.com").orElseThrow().senhaHash());
        var login = entrar("ana@exemplo.com", SENHA, token(null));
        assertEquals(401, login.getResponse().getStatus());
        assertEquals("E-mail ou senha inválidos.", json.readTree(login.getResponse().getContentAsString()).path("mensagem").asText());
    }

    @Test void logoutRealRequerCsrfEInvalidaIdentidade() throws Exception {
        var dados = dados("ESTUDANTE");
        cadastrar(dados).andExpect(status().isCreated());
        var login = entrar(dados.get("email").toString(), SENHA, token(null));
        assertEquals(200, login.getResponse().getStatus());
        var sessao = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(post("/logout").session(sessao)).andExpect(status().isForbidden());
        Token t = token(sessao);
        mvc.perform(post("/logout").session(sessao).header(t.cabecalho(), t.valor()))
            .andExpect(status().is3xxRedirection());
        assertTrue(sessao.isInvalid());
        mvc.perform(get("/api/auth/eu")).andExpect(status().isUnauthorized());
    }
}

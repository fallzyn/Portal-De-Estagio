import app.PortalApplication;
import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import repository.*;
import static org.junit.jupiter.api.Assertions.*;

// HTTP real, autenticação real, cookies e CSRF reais. Fixtures somente no PostgreSQL local.
@SpringBootTest(classes = PortalApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FluxosRestIT extends BancoPostgresTestSupport {
    @Autowired UsuarioRepository usuarios;
    @Autowired AreaRepository areas;
    @Autowired VagaRepository vagas;
    @Autowired CandidaturaRepository candidaturas;
    @Autowired PasswordEncoder encoder;
    @Autowired PlatformTransactionManager transacoes;
    @Autowired ObjectMapper json;
    @LocalServerPort int porta;
    final String sufixo = UUID.randomUUID().toString();
    final String senha = "ApenasTeste#123";
    long admin, empresa, outra, aluno, outroAluno, area;
    String email(String perfil) { return perfil + "." + sufixo + "@exemplo.com"; }
    Usuario usuario(String perfil, Perfil tipo) {
        var u = new Usuario(0, perfil, email(perfil), tipo,
            tipo == Perfil.ESTUDANTE ? "SI" : null, tipo == Perfil.EMPRESA ? perfil : null);
        return u;
    }
    @BeforeEach void preparar() {
        new TransactionTemplate(transacoes).executeWithoutResult(s -> {
            String hash = encoder.encode(senha);
            var lista = List.of(usuario("admin", Perfil.ADMINISTRADOR), usuario("empresa", Perfil.EMPRESA),
                usuario("outra", Perfil.EMPRESA), usuario("aluno", Perfil.ESTUDANTE), usuario("outro", Perfil.ESTUDANTE));
            lista.forEach(u -> { u.definirSenhaHash(hash); usuarios.saveAndFlush(u); });
            admin=lista.get(0).id(); empresa=lista.get(1).id(); outra=lista.get(2).id(); aluno=lista.get(3).id(); outroAluno=lista.get(4).id();
            area=areas.saveAndFlush(new Area(0, "REST " + sufixo)).id();
        });
    }
    @AfterEach void limpar() {
        new TransactionTemplate(transacoes).executeWithoutResult(s -> {
            var idsUsuarios = List.of(admin, empresa, outra, aluno, outroAluno);
            var vagasTeste = vagas.findAll().stream().filter(v -> idsUsuarios.contains(v.empresaId())).toList();
            var idsVagas = vagasTeste.stream().map(Vaga::id).toList();
            candidaturas.deleteAll(candidaturas.findAll().stream().filter(c -> idsVagas.contains(c.vagaId())).toList());
            candidaturas.flush(); vagas.deleteAll(vagasTeste); vagas.flush();
            areas.deleteAll(areas.findAll().stream().filter(a -> a.nome().contains(sufixo)).toList()); areas.flush();
            usuarios.deleteAllById(idsUsuarios);
        });
    }
    class Cliente {
        HttpClient http = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .followRedirects(HttpClient.Redirect.NEVER).build();
        HttpResponse<String> enviar(String metodo, String path, String corpo, String tipo, boolean csrf) throws Exception {
            var b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + porta + path)).header("Accept", "application/json");
            if (csrf) {
                var t = json.readTree(enviar("GET", "/api/auth/csrf", null, null, false).body());
                b.header(t.path("headerName").asText(), t.path("token").asText());
            }
            if (tipo != null) b.header("Content-Type", tipo);
            b.method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corpo));
            return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        }
        HttpResponse<String> pedir(String metodo, String path, Object dados) throws Exception {
            return enviar(metodo, path, dados == null ? null : json.writeValueAsString(dados),
                dados == null ? null : "application/json", !metodo.equals("GET"));
        }
        void entrar(String perfil) throws Exception {
            var r = enviar("POST", "/api/auth/login", "username=" + URLEncoder.encode(email(perfil), StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(senha, StandardCharsets.UTF_8), "application/x-www-form-urlencoded", true);
            assertEquals(200, r.statusCode(), r.body());
        }
    }
    Cliente cliente(String perfil) throws Exception { var c = new Cliente(); if (perfil != null) c.entrar(perfil); return c; }
    Map<String,Object> vaga() { return new HashMap<>(Map.of("titulo","Estágio REST", "descricao","Descrição REST", "areaId",area,
        "localizacao","SP", "modalidade","REMOTA", "cargaHoraria",30)); }
    long publicar(Cliente c) throws Exception {
        var r=c.pedir("POST","/api/empresa/vagas", vaga()); assertEquals(201,r.statusCode(),r.body()); return json.readTree(r.body()).path("id").asLong();
    }
    @Test void publicarEditarListarSemAceitarDonoDoCliente() throws Exception {
        var c=cliente("empresa"); long id=publicar(c);
        var dados=vaga();dados.put("titulo","Editada");
        assertEquals(204,c.pedir("PUT","/api/empresa/vagas/"+id,dados).statusCode());
        var lista=json.readTree(c.pedir("GET","/api/empresa/vagas?empresa="+outra,null).body());
        assertEquals(1,lista.size()); assertEquals(empresa,lista.get(0).path("empresaId").asLong());
        assertEquals("Editada",lista.get(0).path("titulo").asText());
        assertFalse(lista.toString().contains("senha")); assertFalse(lista.toString().contains("@exemplo.com"));
        dados.put("empresaId",outra);
        assertEquals(400,c.pedir("POST","/api/empresa/vagas",dados).statusCode());
    }
    @Test void entradasInvalidasNaoPublicamVagas() throws Exception {
        var c=cliente("empresa");
        for (Object carga : List.of(0,-1,"abc")) {
            var dados=vaga();dados.put("cargaHoraria",carga);
            assertEquals(400,c.pedir("POST","/api/empresa/vagas",dados).statusCode());
        }
        var dados=vaga();dados.remove("titulo"); assertEquals(400,c.pedir("POST","/api/empresa/vagas",dados).statusCode());
        dados=vaga();dados.put("titulo"," ");assertEquals(400,c.pedir("POST","/api/empresa/vagas",dados).statusCode());
        dados=vaga();dados.put("areaId",Long.MAX_VALUE);assertEquals(404,c.pedir("POST","/api/empresa/vagas",dados).statusCode());
        assertEquals(0,json.readTree(c.pedir("GET","/api/empresa/vagas",null).body()).size());
    }
    @Test void outraEmpresaNaoEditaEncerraOuConsultaCandidatos() throws Exception {
        var a=cliente("empresa");long id=publicar(a);var b=cliente("outra");
        assertEquals(403,b.pedir("PUT","/api/empresa/vagas/"+id,vaga()).statusCode());
        assertEquals(403,b.pedir("PATCH","/api/empresa/vagas/"+id+"/encerramento",null).statusCode());
        assertEquals(403,b.pedir("GET","/api/empresa/vagas/"+id+"/candidatos",null).statusCode());
        assertEquals(0,json.readTree(b.pedir("GET","/api/empresa/vagas",null).body()).size());
        assertEquals("ABERTA",json.readTree(a.pedir("GET","/api/empresa/vagas",null).body()).get(0).path("situacao").asText());
    }
    @Test void candidaturaDuplicadaEncerradaEIsolamentoDeEstudantes() throws Exception {
        var e=cliente("empresa");long id=publicar(e);var a=cliente("aluno");var b=cliente("outro");
        assertEquals(201,a.pedir("POST","/api/estudante/candidaturas",Map.of("vagaId",id)).statusCode());
        assertEquals(409,a.pedir("POST","/api/estudante/candidaturas",Map.of("vagaId",id)).statusCode());
        assertEquals(0,json.readTree(b.pedir("GET","/api/estudante/candidaturas?estudante="+aluno,null).body()).size());
        var indevida=Map.of("vagaId",id,"estudanteId",outroAluno);
        assertEquals(400,a.pedir("POST","/api/estudante/candidaturas",indevida).statusCode());
        assertEquals(204,e.pedir("PATCH","/api/empresa/vagas/"+id+"/encerramento",null).statusCode());
        assertEquals(409,b.pedir("POST","/api/estudante/candidaturas",Map.of("vagaId",id)).statusCode());
        var minhas=json.readTree(a.pedir("GET","/api/estudante/candidaturas",null).body());
        assertEquals(1,minhas.size());assertEquals("ENCERRADA",minhas.get(0).path("vaga").path("situacao").asText());
        var candidatos=e.pedir("GET","/api/empresa/vagas/"+id+"/candidatos",null);
        assertEquals(email("aluno"),json.readTree(candidatos.body()).get(0).path("email").asText());
        assertFalse(candidatos.body().contains("senha"));
        assertEquals(409,e.pedir("PUT","/api/empresa/vagas/"+id,vaga()).statusCode());
    }
    @Test void administradorGerenciaAreasEAreaEmUsoNaoPodeSerExcluida() throws Exception {
        var a=cliente("admin");var e=cliente("empresa");publicar(e);
        var r=a.pedir("POST","/api/admin/areas",Map.of("nome","Livre "+sufixo));assertEquals(201,r.statusCode(),r.body());
        long id=json.readTree(r.body()).path("id").asLong();
        assertEquals(409,a.pedir("POST","/api/admin/areas",Map.of("nome","Livre "+sufixo)).statusCode());
        assertEquals(204,a.pedir("PUT","/api/admin/areas/"+id,Map.of("nome","Editada "+sufixo)).statusCode());
        assertEquals(204,a.pedir("DELETE","/api/admin/areas/"+id,null).statusCode());
        assertEquals(409,a.pedir("DELETE","/api/admin/areas/"+area,null).statusCode());
        assertEquals(404,a.pedir("DELETE","/api/admin/areas/"+Long.MAX_VALUE,null).statusCode());
        assertEquals(200,a.pedir("GET","/api/admin/contagens",null).statusCode());
        assertEquals(403,e.pedir("POST","/api/admin/areas",Map.of("nome","Intrusão")).statusCode());
    }
    @Test void perfilAutenticacaoECsrfProtegemRotasDiretas() throws Exception {
        var anon=cliente(null);var estudante=cliente("aluno");var empresa=cliente("empresa");
        assertEquals(401,anon.pedir("POST","/api/empresa/vagas",vaga()).statusCode());
        assertEquals(401,anon.pedir("GET","/api/estudante/candidaturas",null).statusCode());
        assertEquals(403,estudante.pedir("POST","/api/empresa/vagas",vaga()).statusCode());
        assertEquals(403,empresa.pedir("POST","/api/estudante/candidaturas",Map.of("vagaId",1)).statusCode());
        assertEquals(403,empresa.enviar("POST","/api/empresa/vagas",json.writeValueAsString(vaga()),"application/json",false).statusCode());
        assertEquals(403,estudante.enviar("POST","/api/estudante/candidaturas","{\"vagaId\":1}","application/json",false).statusCode());
        assertEquals(302,estudante.pedir("POST","/logout",null).statusCode());
        assertEquals(401,estudante.pedir("GET","/api/auth/eu",null).statusCode());
    }
}

import dao.*;
import model.*;
import service.PortalService;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class RegrasTest {
    private static final class UsuariosFake implements UsuarioDAO {
        public Usuario salvar(String n,String e,Perfil p,String c,String emp){
            return null;
        }
        public Usuario buscar(long id){
            return switch((int)id){
                case 1->new Usuario(1,"Aluno","a@a.com",Perfil.ESTUDANTE,"SI",null);
                case 2->new Usuario(2,"Empresa A","b@b.com",Perfil.EMPRESA,null,"A");
                case 3->new Usuario(3,"Empresa B","c@c.com",Perfil.EMPRESA,null,"B");
                default->null;
            }
            ;
        }
        public List<Usuario> listar(){
            return List.of();
        }
    }
    private static final class AreasFake implements AreaDAO {
        public Area salvar(String n){
            return null;
        }
        public void editar(long id,String n){
        }
        public void excluir(long id){
        }
        public Area buscar(long id){
            return new Area(id,"Tecnologia");
        }
        public List<Area> listar(){
            return List.of();
        }
    }
    private static final class VagasFake implements VagaDAO {
        Vaga vaga=new Vaga(10,2,"Estágio","Descrição",1,"SP",Modalidade.REMOTA,30,Situacao.ABERTA);
        public Vaga salvar(Vaga v){
            return v;
        }
        public Vaga buscar(long id){
            return vaga;
        }
        public void editar(Vaga v){
        }
        public void encerrar(long id){
            vaga=new Vaga(10,2,"Estágio","Descrição",1,"SP",Modalidade.REMOTA,30,Situacao.ENCERRADA);
        }
        public List<Vaga> listarAbertas(Long a,Modalidade m){
            return List.of(vaga);
        }
        public List<Vaga> listarPorEmpresa(long id){
            return List.of(vaga);
        }
    }
    private static final class CandidaturasFake implements CandidaturaDAO {
        boolean existe=false;
        public Candidatura salvar(long estudante,long vaga)throws SQLException{
            if(existe)throw new SQLException("Candidatura duplicada");
            existe=true;
            return new Candidatura(1,estudante,vaga,java.time.LocalDate.now());
        }
        public List<Candidatura> porEstudante(long id){
            return List.of();
        }
        public List<Usuario> candidatos(long id){
            return List.of();
        }
    }
    private final VagasFake vagas=new VagasFake();
    private final PortalService service=new PortalService(new UsuariosFake(),new AreasFake(),vagas,new CandidaturasFake(),()->new Contagens(0,0,0,0,0));
    @Test void candidaturaDuplicada()throws Exception{
        service.candidatar(1,10);
        assertThrows(SQLException.class,()->service.candidatar(1,10));
    }
    @Test void vagaEncerrada()throws Exception{
        service.encerrar(2,10);
        assertThrows(IllegalArgumentException.class,()->service.candidatar(1,10));
    }
    @Test void vagaDeOutraEmpresa() {
        assertThrows(IllegalArgumentException.class,()->service.editarVaga(3,10,"Outro título","Descrição",1,"SP",Modalidade.REMOTA,20));
        assertThrows(IllegalArgumentException.class,()->service.encerrar(3,10));
        assertThrows(IllegalArgumentException.class,()->service.candidatos(3,10));
    }
    @Test void perfilNuloNaoPodeCadastrar() {
        assertThrows(IllegalArgumentException.class,()->service.cadastrar("Nome","nome@exemplo.com",null,null,"Empresa"));
    }
    @Test void cadastroPublicoNaoCriaAdministrador() {
        assertThrows(IllegalArgumentException.class,()->service.cadastrar("Nome","nome@exemplo.com",Perfil.ADMINISTRADOR,null,null));
    }
    @Test void somenteEmpresaPublica() {
        assertThrows(IllegalArgumentException.class,()->service.publicar(1,"Estágio","Descrição",1,"SP",Modalidade.REMOTA,30));
    }
    @Test void somenteEstudanteSeCandidata() {
        assertThrows(IllegalArgumentException.class,()->service.candidatar(2,10));
    }
    @Test void somenteAdministradorAlteraAreasEConsultaContagens() {
        assertThrows(IllegalArgumentException.class,()->service.criarArea(1,"Design"));
        assertThrows(IllegalArgumentException.class,()->service.editarArea(2,1,"Design"));
        assertThrows(IllegalArgumentException.class,()->service.excluirArea(1,1));
        assertThrows(IllegalArgumentException.class,()->service.contagens(2));
    }
    @Test void vagaSemCamposObrigatoriosNaoPodeSerPublicada() {
        assertThrows(IllegalArgumentException.class,()->service.publicar(2," ","Descrição",1,"SP",Modalidade.REMOTA,30));
        assertThrows(IllegalArgumentException.class,()->service.publicar(2,"Estágio"," ",1,"SP",Modalidade.REMOTA,30));
        assertThrows(IllegalArgumentException.class,()->service.publicar(2,"Estágio","Descrição",1," ",Modalidade.REMOTA,30));
        assertThrows(IllegalArgumentException.class,()->service.publicar(2,"Estágio","Descrição",1,"SP",null,30));
        assertThrows(IllegalArgumentException.class,()->service.publicar(2,"Estágio","Descrição",1,"SP",Modalidade.REMOTA,0));
        assertThrows(IllegalArgumentException.class,()->service.publicar(2,"Estágio","Descrição",1,"SP",Modalidade.REMOTA,-1));
    }
    @Test void vagaEncerradaNaoPodeSerEditadaOuReaberta() throws Exception {
        service.encerrar(2,10);
        assertThrows(IllegalArgumentException.class,()->service.editarVaga(2,10,"Outro título","Descrição",1,"SP",Modalidade.REMOTA,20));
        assertThrows(IllegalArgumentException.class,()->service.encerrar(2,10));
        assertEquals(Situacao.ENCERRADA,vagas.vaga.situacao());
    }
    @Test void consultaPublicaNaoMostraVagaEncerrada() throws Exception {
        service.encerrar(2,10);
        assertTrue(service.abertas(null,null).isEmpty());
    }
}

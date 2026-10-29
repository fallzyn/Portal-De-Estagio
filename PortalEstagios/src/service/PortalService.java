package service;
import dao.*;
import model.*;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;
public class PortalService {
    private final UsuarioDAO usuarios;
    private final AreaDAO areas;
    private final VagaDAO vagas;
    private final CandidaturaDAO candidaturas;
    private final EstatisticaDAO estatisticas;
    public PortalService(UsuarioDAO u,AreaDAO a,VagaDAO v,CandidaturaDAO c,EstatisticaDAO e){
        usuarios=u;
        areas=a;
        vagas=v;
        candidaturas=c;
        estatisticas=e;
    }
    private static void texto(String valor,String campo){
        if(valor==null||valor.isBlank())throw new IllegalArgumentException(campo+" é obrigatório.");
    }
    private Usuario exigir(long id,Perfil perfil)throws SQLException{
        Usuario u=usuarios.buscar(id);
        if(u==null||u.perfil()!=perfil)throw new IllegalArgumentException("Operação não permitida para este perfil.");
        return u;
    }
    private Vaga vaga(long id)throws SQLException{
        Vaga v=vagas.buscar(id);
        if(v==null)throw new IllegalArgumentException("Vaga inexistente.");
        return v;
    }
    private Vaga propria(long empresa,long id)throws SQLException{
        exigir(empresa,Perfil.EMPRESA);
        Vaga v=vaga(id);
        if(v.empresaId()!=empresa)throw new IllegalArgumentException("A vaga pertence a outra empresa.");
        return v;
    }
    public Usuario cadastrar(String nome,String email,Perfil perfil,String curso,String empresa)throws SQLException{
        if(perfil==null)throw new IllegalArgumentException("Perfil é obrigatório.");
        texto(nome,"Nome");
        texto(email,"E-mail");
        if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new IllegalArgumentException("E-mail inválido.");
        if(perfil==Perfil.ADMINISTRADOR)throw new IllegalArgumentException("Cadastro de administrador não permitido pelo menu.");
        if(perfil==Perfil.ESTUDANTE)texto(curso,"Curso");
        else texto(empresa,"Nome da empresa");
        return usuarios.salvar(nome,email,perfil,perfil==Perfil.ESTUDANTE?curso:null,perfil==Perfil.EMPRESA?empresa:null);
    }
    public List<Usuario> usuarios()throws SQLException{
        return usuarios.listar();
    }
    public Area criarArea(long admin,String nome)throws SQLException{
        exigir(admin,Perfil.ADMINISTRADOR);
        texto(nome,"Nome da área");
        return areas.salvar(nome);
    }
    public void editarArea(long admin,long id,String nome)throws SQLException{
        exigir(admin,Perfil.ADMINISTRADOR);
        texto(nome,"Nome da área");
        areas.editar(id,nome);
    }
    public void excluirArea(long admin,long id)throws SQLException{
        exigir(admin,Perfil.ADMINISTRADOR);
        areas.excluir(id);
    }
    public List<Area> areas()throws SQLException{
        return areas.listar();
    }
    private void validarVaga(Vaga v)throws SQLException{
        texto(v.titulo(),"Título");
        texto(v.descricao(),"Descrição");
        texto(v.localizacao(),"Localização");
        if(v.modalidade()==null)throw new IllegalArgumentException("Modalidade obrigatória.");
        if(v.cargaHoraria()<=0)throw new IllegalArgumentException("Carga horária deve ser positiva.");
        if(areas.buscar(v.areaId())==null)throw new IllegalArgumentException("Área inexistente.");
    }
    public Vaga publicar(long empresa,String titulo,String descricao,long area,String local,Modalidade modalidade,int carga)throws SQLException{
        exigir(empresa,Perfil.EMPRESA);
        Vaga v=new Vaga(0,empresa,titulo,descricao,area,local,modalidade,carga,Situacao.ABERTA);
        validarVaga(v);
        return vagas.salvar(v);
    }
    public void editarVaga(long empresa,long id,String titulo,String descricao,long area,String local,Modalidade modalidade,int carga)throws SQLException{
        Vaga atual=propria(empresa,id);
        if(atual.situacao()==Situacao.ENCERRADA)throw new IllegalArgumentException("Vaga encerrada não pode ser editada.");
        Vaga v=new Vaga(id,empresa,titulo,descricao,area,local,modalidade,carga,atual.situacao());
        validarVaga(v);
        vagas.editar(v);
    }
    public void encerrar(long empresa,long id)throws SQLException{
        Vaga v=propria(empresa,id);
        if(v.situacao()==Situacao.ENCERRADA)throw new IllegalArgumentException("Vaga já encerrada.");
        vagas.encerrar(id);
    }
    public List<Vaga> abertas(Long area,Modalidade modalidade)throws SQLException{
        List<Vaga> l=vagas.listarAbertas(area,modalidade);
        Predicate<Vaga> aberta=v->v.situacao()==Situacao.ABERTA;
        return l.stream().filter(aberta).toList();
    }
    public List<Vaga> vagasEmpresa(long empresa)throws SQLException{
        exigir(empresa,Perfil.EMPRESA);
        return vagas.listarPorEmpresa(empresa);
    }
    public Candidatura candidatar(long estudante,long idVaga)throws SQLException{
        exigir(estudante,Perfil.ESTUDANTE);
        Vaga v=vaga(idVaga);
        if(v.situacao()!=Situacao.ABERTA)throw new IllegalArgumentException("Vaga encerrada.");
        return candidaturas.salvar(estudante,idVaga);
    }
    public List<Candidatura> minhas(long estudante)throws SQLException{
        exigir(estudante,Perfil.ESTUDANTE);
        return candidaturas.porEstudante(estudante);
    }
    public List<Usuario> candidatos(long empresa,long vaga)throws SQLException{
        propria(empresa,vaga);
        return candidaturas.candidatos(vaga);
    }
    public Contagens contagens(long admin)throws SQLException{
        exigir(admin,Perfil.ADMINISTRADOR);
        return estatisticas.obter();
    }
}

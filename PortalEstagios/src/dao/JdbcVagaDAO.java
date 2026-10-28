package dao;
import java.sql.*;
import java.util.*;
import model.*;
public class JdbcVagaDAO implements VagaDAO {
    private Vaga map(ResultSet r)throws SQLException{
        return new Vaga(r.getLong("id"),r.getLong("empresa_id"),r.getString("titulo"),r.getString("descricao"),r.getLong("area_id"),r.getString("localizacao"),Modalidade.valueOf(r.getString("modalidade")),r.getInt("carga_horaria"),Situacao.valueOf(r.getString("situacao")));
    }
    public Vaga salvar(Vaga v)throws SQLException{
        String sql="INSERT INTO vagas(empresa_id,titulo,descricao,area_id,localizacao,modalidade,carga_horaria,situacao) VALUES(?,?,?,?,?,?,?,?) RETURNING *";
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement(sql)){
            preencher(ps,v);
            try(ResultSet r=ps.executeQuery()){
                r.next();
                return map(r);
            }
        }
    }
    private void preencher(PreparedStatement ps,Vaga v)throws SQLException{
        ps.setLong(1,v.empresaId());
        ps.setString(2,v.titulo());
        ps.setString(3,v.descricao());
        ps.setLong(4,v.areaId());
        ps.setString(5,v.localizacao());
        ps.setString(6,v.modalidade().name());
        ps.setInt(7,v.cargaHoraria());
        ps.setString(8,v.situacao().name());
    }
    public Vaga buscar(long id)throws SQLException{
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("SELECT * FROM vagas WHERE id=?")){
            ps.setLong(1,id);
            try(ResultSet r=ps.executeQuery()){
                return r.next()?map(r):null;
            }
        }
    }
    public void editar(Vaga v)throws SQLException{
        String sql="UPDATE vagas SET empresa_id=?,titulo=?,descricao=?,area_id=?,localizacao=?,modalidade=?,carga_horaria=?,situacao=? WHERE id=?";
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement(sql)){
            preencher(ps,v);
            ps.setLong(9,v.id());
            if(ps.executeUpdate()==0)throw new IllegalArgumentException("Vaga inexistente.");
        }
    }
    public void encerrar(long id)throws SQLException{
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("UPDATE vagas SET situacao='ENCERRADA' WHERE id=?")){
            ps.setLong(1,id);
            if(ps.executeUpdate()==0)throw new IllegalArgumentException("Vaga inexistente.");
        }
    }
    public List<Vaga> listarAbertas(Long area,Modalidade modalidade)throws SQLException{
        String sql="SELECT * FROM vagas WHERE situacao='ABERTA' AND (CAST(? AS BIGINT) IS NULL OR area_id=?) AND (CAST(? AS VARCHAR) IS NULL OR modalidade=?) ORDER BY id";
        List<Vaga> l=new ArrayList<>();
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement(sql)){
            if(area==null){
                ps.setNull(1,Types.BIGINT);
                ps.setNull(2,Types.BIGINT);
            }
            else{
                ps.setLong(1,area);
                ps.setLong(2,area);
            }
            ps.setString(3,modalidade==null?null:modalidade.name());
            ps.setString(4,modalidade==null?null:modalidade.name());
            try(ResultSet r=ps.executeQuery()){
                while(r.next())l.add(map(r));
            }
        }
        return l;
    }
    public List<Vaga> listarPorEmpresa(long id)throws SQLException{
        List<Vaga> l=new ArrayList<>();
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("SELECT * FROM vagas WHERE empresa_id=? ORDER BY id")){
            ps.setLong(1,id);
            try(ResultSet r=ps.executeQuery()){
                while(r.next())l.add(map(r));
            }
        }
        return l;
    }
}

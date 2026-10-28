package dao;
import java.sql.*;
import java.util.*;
import model.Area;
public class JdbcAreaDAO implements AreaDAO {
    private Area map(ResultSet r)throws SQLException{
        return new Area(r.getLong("id"),r.getString("nome"));
    }
    public Area salvar(String nome)throws SQLException{
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("INSERT INTO areas(nome) VALUES(?) RETURNING *")){
            ps.setString(1,nome);
            try(ResultSet r=ps.executeQuery()){
                r.next();
                return map(r);
            }
        }
    }
    public void editar(long id,String nome)throws SQLException{
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("UPDATE areas SET nome=? WHERE id=?")){
            ps.setString(1,nome);
            ps.setLong(2,id);
            if(ps.executeUpdate()==0)throw new IllegalArgumentException("Área inexistente.");
        }
    }
    public void excluir(long id)throws SQLException{
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("DELETE FROM areas WHERE id=?")){
            ps.setLong(1,id);
            if(ps.executeUpdate()==0)throw new IllegalArgumentException("Área inexistente.");
        }
    }
    public Area buscar(long id)throws SQLException{
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("SELECT * FROM areas WHERE id=?")){
            ps.setLong(1,id);
            try(ResultSet r=ps.executeQuery()){
                return r.next()?map(r):null;
            }
        }
    }
    public List<Area> listar()throws SQLException{
        List<Area> l=new ArrayList<>();
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("SELECT * FROM areas ORDER BY nome");
        ResultSet r=ps.executeQuery()){
            while(r.next())l.add(map(r));
        }
        return l;
    }
}

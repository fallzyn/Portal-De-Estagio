package dao;
import java.sql.*;
import java.util.*;
import model.*;
public class JdbcUsuarioDAO implements UsuarioDAO {
    private Usuario map(ResultSet r)throws SQLException{
        return new Usuario(r.getLong("id"),r.getString("nome"),r.getString("email"),Perfil.valueOf(r.getString("perfil")),r.getString("curso"),r.getString("nome_empresa"));
    }
    public Usuario salvar(String nome,String email,Perfil perfil,String curso,String empresa)throws SQLException{
        String sql="INSERT INTO usuarios(nome,email,perfil,curso,nome_empresa) VALUES(?,?,?,?,?) RETURNING *";
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,nome);
            ps.setString(2,email);
            ps.setString(3,perfil.name());
            ps.setString(4,curso);
            ps.setString(5,empresa);
            try(ResultSet r=ps.executeQuery()){
                r.next();
                return map(r);
            }
        }
    }
    public Usuario buscar(long id)throws SQLException{
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("SELECT * FROM usuarios WHERE id=?")){
            ps.setLong(1,id);
            try(ResultSet r=ps.executeQuery()){
                return r.next()?map(r):null;
            }
        }
    }
    public List<Usuario> listar()throws SQLException{
        List<Usuario> lista=new ArrayList<>();
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("SELECT * FROM usuarios ORDER BY id");
        ResultSet r=ps.executeQuery()){
            while(r.next())lista.add(map(r));
        }
        return lista;
    }
}

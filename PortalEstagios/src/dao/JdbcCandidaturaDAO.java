package dao;
import java.sql.*;
import java.util.*;
import model.*;
public class JdbcCandidaturaDAO implements CandidaturaDAO {
    private Candidatura map(ResultSet r) throws SQLException {

    return new Candidatura(
        r.getLong("id"),
        r.getLong("estudante_id"),
        r.getLong("vaga_id"),
        r.getDate("data_inscricao").toLocalDate()
    );
}
    public Candidatura salvar(long estudante,long vaga)throws SQLException{
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("INSERT INTO candidaturas(estudante_id,vaga_id) VALUES(?,?) RETURNING *")){
            ps.setLong(1,estudante);
            ps.setLong(2,vaga);
            try(ResultSet r=ps.executeQuery()){
                r.next();
                return map(r);
            }
        }
    }
    public List<Candidatura> porEstudante(long estudante)throws SQLException{
        List<Candidatura> l=new ArrayList<>();
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement("SELECT * FROM candidaturas WHERE estudante_id=? ORDER BY data_inscricao DESC")){
            ps.setLong(1,estudante);
            try(ResultSet r=ps.executeQuery()){
                while(r.next())l.add(map(r));
            }
        }
        return l;
    }
    public List<Usuario> candidatos(long vaga)throws SQLException{
        List<Usuario> l=new ArrayList<>();
        String sql="SELECT u.* FROM usuarios u JOIN candidaturas c ON c.estudante_id=u.id WHERE c.vaga_id=? ORDER BY c.data_inscricao";
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement(sql)){
            ps.setLong(1,vaga);
            try(ResultSet r=ps.executeQuery()){
                while(r.next())l.add(new Usuario(r.getLong("id"),r.getString("nome"),r.getString("email"),Perfil.valueOf(r.getString("perfil")),r.getString("curso"),r.getString("nome_empresa")));
            }
        }
        return l;
    }
}

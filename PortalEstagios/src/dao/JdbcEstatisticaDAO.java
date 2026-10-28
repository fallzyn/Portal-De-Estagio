package dao;
import java.sql.*;
import model.Contagens;
public class JdbcEstatisticaDAO implements EstatisticaDAO {
    public Contagens obter()throws SQLException{
        String sql="SELECT (SELECT COUNT(*) FROM usuarios WHERE perfil='ESTUDANTE') e,(SELECT COUNT(*) FROM usuarios WHERE perfil='EMPRESA') emp,(SELECT COUNT(*) FROM vagas WHERE situacao='ABERTA') a,(SELECT COUNT(*) FROM vagas WHERE situacao='ENCERRADA') enc,(SELECT COUNT(*) FROM candidaturas) c";
        try(Connection c=Conexao.abrir();
        PreparedStatement ps=c.prepareStatement(sql);
        ResultSet r=ps.executeQuery()){
            r.next();
            return new Contagens(r.getLong("e"),r.getLong("emp"),r.getLong("a"),r.getLong("enc"),r.getLong("c"));
        }
    }
}

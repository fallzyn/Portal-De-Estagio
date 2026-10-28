package dao;
import java.sql.SQLException;
import model.Contagens;
public interface EstatisticaDAO {
    Contagens obter() throws SQLException;
}

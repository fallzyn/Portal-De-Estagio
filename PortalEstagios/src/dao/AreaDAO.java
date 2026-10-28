package dao;
import java.sql.SQLException;
import java.util.List;
import model.Area;
public interface AreaDAO {
    Area salvar(String nome) throws SQLException;
    void editar(long id,String nome) throws SQLException;
    void excluir(long id) throws SQLException;
    Area buscar(long id) throws SQLException;
    List<Area> listar() throws SQLException;
}

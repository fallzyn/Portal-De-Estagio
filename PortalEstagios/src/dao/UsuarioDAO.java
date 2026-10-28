package dao;
import java.sql.SQLException;
import java.util.List;
import model.*;
public interface UsuarioDAO {
    Usuario salvar(String nome,String email,Perfil perfil,String curso,String empresa) throws SQLException;
    Usuario buscar(long id) throws SQLException;
    List<Usuario> listar() throws SQLException;
}

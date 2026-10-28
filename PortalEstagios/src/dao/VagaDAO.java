package dao;
import java.sql.SQLException;
import java.util.List;
import model.*;
public interface VagaDAO {
    Vaga salvar(Vaga vaga) throws SQLException;
    Vaga buscar(long id) throws SQLException;
    void editar(Vaga vaga) throws SQLException;
    void encerrar(long id) throws SQLException;
    List<Vaga> listarAbertas(Long area,Modalidade modalidade) throws SQLException;
    List<Vaga> listarPorEmpresa(long empresaId) throws SQLException;
}

package dao;
import java.sql.SQLException;
import java.util.List;
import model.*;
public interface CandidaturaDAO {
    Candidatura salvar(long estudante,long vaga) throws SQLException;
    List<Candidatura> porEstudante(long estudante) throws SQLException;
    List<Usuario> candidatos(long vaga) throws SQLException;
}

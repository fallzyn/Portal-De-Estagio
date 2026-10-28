package dao;
import java.sql.*;
public final class Conexao {
    private Conexao() {
    }
    public static Connection abrir() throws SQLException {
        String url=System.getenv().getOrDefault("DB_URL","jdbc:postgresql://SEU_HOST:5432/postgres?sslmode=require");
        String user=System.getenv().getOrDefault("DB_USER","postgres.SEU_PROJETO");
        String senha=System.getenv("DB_PASSWORD");
        if(senha==null) throw new SQLException("Configure a variável DB_PASSWORD.");
        return DriverManager.getConnection(url,user,senha);
    }
}

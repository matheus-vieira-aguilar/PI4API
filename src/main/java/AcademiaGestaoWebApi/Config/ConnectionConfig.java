package AcademiaGestaoWebApi.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Gabriel
 */
@Component
public class ConnectionConfig {

    private static String url;

    private static String username;

    private static String password;

    public static Connection getConnection(boolean autoCommit) {
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(autoCommit);
            return connection;
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao estabeler conexão: " + ex);
        }
    }

    public static void closeConnection(Connection con) {

        if (con != null) {
            try {
                con.close();

            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexão: " + ex);
            }
        }
    }

    public static void closeConnection(Connection con, PreparedStatement stmt) {

        closeConnection(con);

        try {
            if (stmt != null) {
                stmt.close();
            }

        } catch (SQLException ex) {
            System.err.println("Erro ao fechar conexão: " + ex);
        }
    }

    public static void closeConnection(Connection con, PreparedStatement stmt, ResultSet rs) {

        closeConnection(con, stmt);
        try {
            if (rs != null) {
                rs.close();
            }

        } catch (SQLException ex) {
            System.err.println("Erro ao fechar conexão: " + ex);
        }
    }

    @Value("${spring.datasource.url}")
    public void setUrl(String url) {
        this.url = url;
    }

    @Value("${spring.datasource.username}")
    public void setUsername(String username) {
        this.username = username;
    }

    @Value("${spring.datasource.password}")
    public void setPassword(String password) {
        this.password = password;
    }
}

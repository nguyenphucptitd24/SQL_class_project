package ktx.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String URL = System.getenv().getOrDefault(
        "KTX_DB_URL",
        "jdbc:mysql://localhost:3306/KTX?serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8"
    );
    private static final String USER = System.getenv().getOrDefault("KTX_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("KTX_DB_PASSWORD", "");

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

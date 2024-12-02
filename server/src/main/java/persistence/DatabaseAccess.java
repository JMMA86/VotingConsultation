package persistence;
import io.github.cdimascio.dotenv.Dotenv;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccess {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    static {
        config.setJdbcUrl(DB_URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    public String getVotingStation(String voterId) throws SQLException {
        try (Connection conn = ds.getConnection()) {
            // Consulta para obtener el puesto de votación del ciudadano
            String query = """
                SELECT pv.nombre AS voting_station, mv.id AS mesa_id
                FROM ciudadano c
                JOIN mesa_votacion mv ON c.mesa_id = mv.id
                JOIN puesto_votacion pv ON mv.puesto_id = pv.id
                WHERE c.documento = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, voterId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "" + rs.getInt("mesa_id");
                // return rs.getString("voting_station") + " (Mesa " + rs.getInt("mesa_id") + ")";
            } else {
                // throw new SQLException("Voter not found");
                return "-1";
            }
        }
    }

    public List<String> getVotingStations(String city) throws SQLException {
        try (Connection conn = ds.getConnection()) {
            // Consulta para obtener todos los puestos de votación en un municipio
            String query = """
                SELECT DISTINCT pv.nombre AS voting_station
                FROM puesto_votacion pv
                JOIN municipio m ON pv.municipio_id = m.id
                WHERE m.nombre = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();
            List<String> stations = new ArrayList<>();
            while (rs.next()) {
                stations.add(rs.getString("voting_station"));
            }
            return stations;
        }
    }
}

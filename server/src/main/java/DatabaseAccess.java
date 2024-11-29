import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccess {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("USER");
    private static final String PASSWORD = dotenv.get("PASSWORD");

    public String getVotingStation(String voterId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // Consulta para obtener el puesto de votación del ciudadano
            String query = """
                SELECT pv.nombre AS voting_station
                FROM ciudadano c
                JOIN mesa_votacion mv ON c.mesa_id = mv.id
                JOIN puesto_votacion pv ON mv.puesto_id = pv.id
                WHERE c.documento = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, voterId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("voting_station");
            } else {
                throw new SQLException("Voter not found");
            }
        }
    }

    public List<String> getVotingStations(String city) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
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

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
            String query = "SELECT voting_station FROM voters WHERE voter_id = ?";
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
            String query = "SELECT DISTINCT voting_station FROM voters WHERE city = ?";
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

    public void saveAuditLog(String voterId, String action, String details) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "INSERT INTO audit_logs (voter_id, action, details, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, voterId);
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.executeUpdate();
        }
    }
}
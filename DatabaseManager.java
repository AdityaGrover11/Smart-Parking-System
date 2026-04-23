import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // SQLite connection string
    private static final String URL = "jdbc:sqlite:parking_data.db";

    public DatabaseManager() {
        createTablesIfNotExist();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private void createTablesIfNotExist() {
        String sqlSlots = "CREATE TABLE IF NOT EXISTS parked_vehicles (\n"
                + "    floor INTEGER,\n"
                + "    slot INTEGER,\n"
                + "    vehicle_number TEXT,\n"
                + "    type TEXT,\n"
                + "    entry_time TEXT,\n"
                + "    PRIMARY KEY (floor, slot)\n"
                + ");";

        String sqlStats = "CREATE TABLE IF NOT EXISTS stats (\n"
                + "    id INTEGER PRIMARY KEY CHECK (id = 1),\n"
                + "    revenue_today INTEGER,\n"
                + "    total_vehicles_today INTEGER\n"
                + ");";

        String sqlHistory = "CREATE TABLE IF NOT EXISTS vehicle_history (\n"
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    vehicle_number TEXT,\n"
                + "    type TEXT,\n"
                + "    entry_time TEXT,\n"
                + "    exit_time TEXT,\n"
                + "    fee_paid REAL\n"
                + ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlSlots);
            stmt.execute(sqlStats);
            stmt.execute(sqlHistory);

            // Initialize stats if empty
            ResultSet rs = stmt.executeQuery("SELECT count(*) AS count FROM stats");
            if (rs.next() && rs.getInt("count") == 0) {
                stmt.execute("INSERT INTO stats (id, revenue_today, total_vehicles_today) VALUES (1, 0, 0)");
            }
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }

    public void saveVehicle(int floor, int slot, String vehicleNumber, String type, LocalDateTime entryTime) {
        String sql = "INSERT OR REPLACE INTO parked_vehicles (floor, slot, vehicle_number, type, entry_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, floor);
            pstmt.setInt(2, slot);
            pstmt.setString(3, vehicleNumber);
            pstmt.setString(4, type);
            // Save time as string ISO format
            pstmt.setString(5, entryTime.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving vehicle: " + e.getMessage());
        }
    }

    public void removeVehicle(int floor, int slot) {
        String sql = "DELETE FROM parked_vehicles WHERE floor = ? AND slot = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, floor);
            pstmt.setInt(2, slot);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing vehicle: " + e.getMessage());
        }
    }

    public void updateStats(long revenueToday, int totalVehiclesToday) {
        String sql = "UPDATE stats SET revenue_today = ?, total_vehicles_today = ? WHERE id = 1";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, revenueToday);
            pstmt.setInt(2, totalVehiclesToday);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating stats: " + e.getMessage());
        }
    }

    // Method to fetch all parked vehicles and populate arrays
    public void loadAllVehicles(String[][] parkedVehicles, String[][] vehicleTypes, LocalDateTime[][] entryTimes) {
        String sql = "SELECT * FROM parked_vehicles";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int f = rs.getInt("floor");
                int s = rs.getInt("slot");
                
                // Safety check so we don't crash if arrays are smaller than DB data
                if (f < parkedVehicles.length && s < parkedVehicles[0].length) {
                    parkedVehicles[f][s] = rs.getString("vehicle_number");
                    vehicleTypes[f][s] = rs.getString("type");
                    entryTimes[f][s] = LocalDateTime.parse(rs.getString("entry_time"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading vehicles: " + e.getMessage());
        }
    }

    // Method to fetch statistics
    public long[] loadStats() {
        String sql = "SELECT revenue_today, total_vehicles_today FROM stats WHERE id = 1";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                long rev = rs.getLong("revenue_today");
                int tot = rs.getInt("total_vehicles_today");
                return new long[]{rev, tot};
            }
        } catch (SQLException e) {
            System.out.println("Error loading stats: " + e.getMessage());
        }
        return new long[]{0, 0};
    }

    public void logHistory(String vehicleNumber, String type, LocalDateTime entryTime, LocalDateTime exitTime, double feePaid) {
        String sql = "INSERT INTO vehicle_history (vehicle_number, type, entry_time, exit_time, fee_paid) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleNumber);
            pstmt.setString(2, type);
            pstmt.setString(3, entryTime.toString());
            pstmt.setString(4, exitTime.toString());
            pstmt.setDouble(5, feePaid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error logging history: " + e.getMessage());
        }
    }

    public List<String[]> queryHistory(String search, String timeFilter) {
        List<String[]> historyData = new ArrayList<>();
        
        // Base query
        String sql = "SELECT * FROM vehicle_history WHERE 1=1";
        
        if (search != null && !search.trim().isEmpty()) {
            sql += " AND vehicle_number LIKE '%" + search.trim().toUpperCase() + "%'";
        }

        if (timeFilter != null) {
            if (timeFilter.equals("Last 1 Week")) {
                sql += " AND exit_time >= '" + LocalDateTime.now().minusWeeks(1).toString() + "'";
            } else if (timeFilter.equals("Last 1 Month")) {
                sql += " AND exit_time >= '" + LocalDateTime.now().minusMonths(1).toString() + "'";
            }
        }
        
        sql += " ORDER BY id DESC"; // Newest first

        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String[] row = {
                    rs.getString("vehicle_number"),
                    rs.getString("type"),
                    rs.getString("entry_time").replace("T", " ").substring(0, 19),
                    rs.getString("exit_time").replace("T", " ").substring(0, 19),
                    "₹" + rs.getDouble("fee_paid")
                };
                historyData.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error querying history: " + e.getMessage());
        }
        return historyData;
    }
}

package gg.auroramc.quests.api.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.auroramc.quests.AuroraQuests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * MySQL backend for global quest data storage.
 * Handles cross-server synchronization through a shared database.
 */
public class GlobalQuestMySQL {
    private HikariDataSource dataSource;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public GlobalQuestMySQL(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    /**
     * Initialize the connection pool and create tables
     */
    public void connect() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);

            createTables();

            AuroraQuests.logger().info("Connected to MySQL for global quest storage");
        } catch (Exception e) {
            AuroraQuests.logger().severe("Failed to connect to MySQL for global quests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Close the connection pool
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            AuroraQuests.logger().info("Disconnected from MySQL global quest storage");
        }
    }

    /**
     * Create necessary database tables
     */
    private void createTables() {
        String progressTable = """
                CREATE TABLE IF NOT EXISTS aurora_global_quest_progress (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    quest_id VARCHAR(64) NOT NULL,
                    task_id VARCHAR(64) NOT NULL,
                    progress BIGINT DEFAULT 0,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_quest_task (quest_id, task_id),
                    INDEX idx_quest (quest_id),
                    INDEX idx_updated (updated_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        String contributionsTable = """
                CREATE TABLE IF NOT EXISTS aurora_global_quest_contributions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    quest_id VARCHAR(64) NOT NULL,
                    player_uuid CHAR(36) NOT NULL,
                    contribution BIGINT DEFAULT 0,
                    last_contributed TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_player_quest (quest_id, player_uuid),
                    INDEX idx_quest_contrib (quest_id, contribution DESC),
                    INDEX idx_player (player_uuid)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        String milestonesTable = """
                CREATE TABLE IF NOT EXISTS aurora_global_quest_milestones (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    quest_id VARCHAR(64) NOT NULL,
                    percentage INT NOT NULL,
                    reached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_quest_milestone (quest_id, percentage),
                    INDEX idx_quest (quest_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute(progressTable);
            conn.createStatement().execute(contributionsTable);
            conn.createStatement().execute(milestonesTable);
            AuroraQuests.logger().info("Global quest database tables created/verified");
        } catch (SQLException e) {
            AuroraQuests.logger().severe("Failed to create global quest tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load global progress from database into GlobalQuestData
     */
    public void loadGlobalData(GlobalQuestData data) {
        try (Connection conn = dataSource.getConnection()) {
            // Load progress
            String progressQuery = "SELECT quest_id, task_id, progress FROM aurora_global_quest_progress";
            int progressCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(progressQuery);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String questId = rs.getString("quest_id");
                    String taskId = rs.getString("task_id");
                    long progress = rs.getLong("progress");
                    data.setProgress(questId, taskId, progress);
                    progressCount++;
                    AuroraQuests.logger().info("[GLOBAL_DEBUG] Loaded progress: " + questId + "/" + taskId + " = " + progress);
                }
            }
            AuroraQuests.logger().info("[GLOBAL_DEBUG] Loaded " + progressCount + " progress entries from database");

            // Load contributions
            String contribQuery = "SELECT quest_id, player_uuid, contribution FROM aurora_global_quest_contributions";
            try (PreparedStatement stmt = conn.prepareStatement(contribQuery);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String questId = rs.getString("quest_id");
                    UUID playerId = UUID.fromString(rs.getString("player_uuid"));
                    long contribution = rs.getLong("contribution");
                    data.getContributions().computeIfAbsent(questId, k -> new java.util.concurrent.ConcurrentHashMap<>())
                            .put(playerId, contribution);
                }
            }

            // Load milestones
            String milestoneQuery = "SELECT quest_id, percentage FROM aurora_global_quest_milestones";
            try (PreparedStatement stmt = conn.prepareStatement(milestoneQuery);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String questId = rs.getString("quest_id");
                    int percentage = rs.getInt("percentage");
                    data.markMilestone(questId, percentage);
                }
            }

            AuroraQuests.logger().info("Loaded global quest data from MySQL");
        } catch (SQLException e) {
            AuroraQuests.logger().severe("Failed to load global quest data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save progress to database
     */
    public void saveProgress(String questId, String taskId, long progress) {
        String query = """
                INSERT INTO aurora_global_quest_progress (quest_id, task_id, progress)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE progress = VALUES(progress), updated_at = CURRENT_TIMESTAMP
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, questId);
            stmt.setString(2, taskId);
            stmt.setLong(3, progress);
            stmt.executeUpdate();
            AuroraQuests.logger().info("[GLOBAL_DEBUG] Saved progress to DB: " + questId + "/" + taskId + " = " + progress);
        } catch (SQLException e) {
            AuroraQuests.logger().severe("Failed to save global progress: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save player contribution to database
     */
    public void saveContribution(String questId, UUID playerId, long contribution) {
        String query = """
                INSERT INTO aurora_global_quest_contributions (quest_id, player_uuid, contribution)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE contribution = VALUES(contribution), last_contributed = CURRENT_TIMESTAMP
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, questId);
            stmt.setString(2, playerId.toString());
            stmt.setLong(3, contribution);
            stmt.executeUpdate();
        } catch (SQLException e) {
            AuroraQuests.logger().severe("Failed to save contribution: " + e.getMessage());
        }
    }

    /**
     * Mark milestone as reached in database
     */
    public void saveMilestone(String questId, int percentage) {
        String query = """
                INSERT IGNORE INTO aurora_global_quest_milestones (quest_id, percentage)
                VALUES (?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, questId);
            stmt.setInt(2, percentage);
            stmt.executeUpdate();
        } catch (SQLException e) {
            AuroraQuests.logger().severe("Failed to save milestone: " + e.getMessage());
        }
    }

    /**
     * Get current progress from database (for polling)
     */
    public long getProgress(String questId, String taskId) {
        String query = "SELECT progress FROM aurora_global_quest_progress WHERE quest_id = ? AND task_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, questId);
            stmt.setString(2, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("progress");
                }
            }
        } catch (SQLException e) {
            AuroraQuests.logger().severe("Failed to get progress: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Reset quest data (admin command)
     */
    public void resetQuest(String questId) {
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute("DELETE FROM aurora_global_quest_progress WHERE quest_id = '" + questId + "'");
            conn.createStatement().execute("DELETE FROM aurora_global_quest_contributions WHERE quest_id = '" + questId + "'");
            conn.createStatement().execute("DELETE FROM aurora_global_quest_milestones WHERE quest_id = '" + questId + "'");
            AuroraQuests.logger().info("Reset global quest: " + questId);
        } catch (SQLException e) {
            AuroraQuests.logger().severe("Failed to reset quest: " + e.getMessage());
        }
    }

    /**
     * Check if connection is alive
     */
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
}

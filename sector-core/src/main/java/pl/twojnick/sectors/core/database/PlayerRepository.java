package pl.twojnick.sectors.core.database;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class PlayerRepository {
    private final HikariDataSource dataSource;
    private final ExecutorService executor;

    public PlayerRepository(HikariDataSource dataSource, ExecutorService executor) {
        this.dataSource = dataSource;
        this.executor = executor;
        initTable();
    }

    private void initTable() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "CREATE TABLE IF NOT EXISTS player_sectors (" +
                 "uuid VARCHAR(36) PRIMARY KEY, " +
                 "last_sector VARCHAR(50), " +
                 "player_rank VARCHAR(50) DEFAULT 'PLAYER')")) {
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to init player_sectors table", e);
        }
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT last_sector, player_rank FROM player_sectors WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new PlayerData(
                        uuid,
                        rs.getString("last_sector"),
                        Rank.fromString(rs.getString("player_rank"))
                    );
                }
                return new PlayerData(uuid, null, Rank.PLAYER);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<Void> updateLastSector(UUID uuid, String sectorId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO player_sectors (uuid, last_sector) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE last_sector = VALUES(last_sector)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, sectorId);
                ps.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<Void> setRank(UUID uuid, Rank rank) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO player_sectors (uuid, player_rank) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE player_rank = VALUES(player_rank)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, rank.name());
                ps.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
}

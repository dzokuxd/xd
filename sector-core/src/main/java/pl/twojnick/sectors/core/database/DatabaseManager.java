package pl.twojnick.sectors.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import pl.twojnick.sectors.core.config.SectorConfig;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {
    private HikariDataSource dataSource;
    private final ExecutorService dbExecutor;

    public DatabaseManager() {
        this.dbExecutor = Executors.newFixedThreadPool(10);
    }

    public void init(SectorConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.dbHost + ":" + config.dbPort + "/" + config.dbName);
        hikariConfig.setUsername(config.dbUser);
        hikariConfig.setPassword(config.dbPassword);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public ExecutorService getExecutor() {
        return dbExecutor;
    }

    public void close() {
        if (dataSource != null) dataSource.close();
        if (dbExecutor != null) dbExecutor.shutdown();
    }
}

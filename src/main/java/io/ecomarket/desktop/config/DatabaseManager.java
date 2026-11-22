package io.ecomarket.desktop.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages the connection pool to Supabase PostgreSQL using HikariCP.
 */
public class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;

    private DatabaseManager() {}

    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        if (!SupabaseConfig.isDbConfigured()) {
            log.info("Supabase DB credentials not configured yet. Application will operate with local in-memory fallback.");
            return;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(SupabaseConfig.getDbUrl());
            config.setUsername(SupabaseConfig.getDbUser());
            config.setPassword(SupabaseConfig.getDbPassword());
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(15000); // 15 seconds
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setPoolName("Supabase-EcoMarket-Pool");

            dataSource = new HikariDataSource(config);
            log.info("HikariCP pool initialized successfully to Supabase PostgreSQL.");
        } catch (Exception e) {
            log.error("Failed to initialize Supabase Hikari pool: {}", e.getMessage());
            dataSource = null;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initialize();
        }
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Supabase database connection pool is not available.");
        }
        return dataSource.getConnection();
    }

    public static boolean isAvailable() {
        if (dataSource == null) {
            initialize();
        }
        if (dataSource == null || dataSource.isClosed()) {
            return false;
        }
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(3);
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("Supabase database pool shut down.");
        }
    }
}

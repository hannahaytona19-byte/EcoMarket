package io.ecomarket.desktop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Automates applying schema.sql directly to Supabase PostgreSQL.
 */
public class DatabaseMigrator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrator.class);

    public static boolean migrate() {
        if (!DatabaseManager.isAvailable()) {
            log.warn("Cannot run migration: Supabase database connection is not available.");
            return false;
        }

        log.info("Running schema migration against Supabase PostgreSQL...");
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            InputStream in = DatabaseMigrator.class.getResourceAsStream("/supabase/schema.sql");
            if (in == null) {
                // Try file system if not in resources
                java.io.File file = new java.io.File("supabase/schema.sql");
                if (file.exists()) {
                    in = new java.io.FileInputStream(file);
                }
            }

            if (in == null) {
                log.error("Could not find supabase/schema.sql file.");
                return false;
            }

            String ddl;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                ddl = reader.lines().collect(Collectors.joining("\n"));
            }

            // Split statements by semicolon
            String[] statements = ddl.split(";");
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    try {
                        stmt.execute(trimmed);
                    } catch (Exception ex) {
                        log.debug("Notice on statement: {} -> {}", trimmed.substring(0, Math.min(trimmed.length(), 40)), ex.getMessage());
                    }
                }
            }

            log.info("Supabase database migration executed successfully!");
            return true;
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            return false;
        }
    }
}

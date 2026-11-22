package io.ecomarket.desktop.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Manages Supabase configuration parameters:
 * Direct PostgreSQL connection (JDBC / Pooler) and Supabase REST/Auth endpoint.
 */
public class SupabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(SupabaseConfig.class);

    private static String supabaseUrl;
    private static String supabaseAnonKey;
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties properties = new Properties();

        // 1. Load from classpath properties if available
        try (InputStream in = SupabaseConfig.class.getResourceAsStream("/supabase.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (Exception e) {
            log.warn("Could not load /supabase.properties: {}", e.getMessage());
        }

        // 2. Load from .env file if available
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            log.debug(".env file not present or unreadable");
        }

        supabaseUrl = resolveValue("SUPABASE_URL", dotenv, properties, "https://your-project.supabase.co");
        supabaseAnonKey = resolveValue("SUPABASE_ANON_KEY", dotenv, properties, "your-anon-key");
        dbUrl = resolveValue("SUPABASE_DB_URL", dotenv, properties, "jdbc:postgresql://db.your-project.supabase.co:5432/postgres?sslmode=require");
        dbUser = resolveValue("SUPABASE_DB_USER", dotenv, properties, "postgres");
        dbPassword = resolveValue("SUPABASE_DB_PASSWORD", dotenv, properties, "");
    }

    private static String resolveValue(String key, Dotenv dotenv, Properties props, String defaultValue) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env;

        if (dotenv != null) {
            String val = dotenv.get(key);
            if (val != null && !val.isBlank()) return val;
        }

        String propVal = props.getProperty(key);
        if (propVal != null && !propVal.isBlank()) return propVal;

        return defaultValue;
    }

    public static String getSupabaseUrl() { return supabaseUrl; }
    public static String getSupabaseAnonKey() { return supabaseAnonKey; }
    public static String getDbUrl() { return dbUrl; }
    public static String getDbUser() { return dbUser; }
    public static String getDbPassword() { return dbPassword; }

    public static boolean isDbConfigured() {
        return dbUrl != null &&
               !dbUrl.contains("your-project") &&
               dbPassword != null &&
               !dbPassword.isBlank();
    }

    public static boolean isRestConfigured() {
        return supabaseUrl != null &&
               !supabaseUrl.contains("your-project") &&
               supabaseAnonKey != null &&
               !supabaseAnonKey.contains("your-anon-key");
    }
}

package projet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central JDBC configuration for the SOKTRAPA application.
 *
 * <p>Database credentials are no longer hardcoded in the Swing classes. They are
 * resolved once, in the following order (first match wins for each key):</p>
 * <ol>
 *   <li>Environment variables {@code SOKTRAPA_DB_URL}, {@code SOKTRAPA_DB_USER},
 *       {@code SOKTRAPA_DB_PASSWORD};</li>
 *   <li>A properties file given with {@code -Dsoktrapa.config=/path/to/file};</li>
 *   <li>{@code config/database.properties} relative to the working directory
 *       (the project root when launched from Eclipse or Maven).</li>
 * </ol>
 *
 * <p>Copy {@code config/database.properties.example} to
 * {@code config/database.properties} and fill in your local values.
 * The real file is excluded from Git by {@code .gitignore}.</p>
 */
public final class DatabaseConfig {

    public static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/soktrapa";
    private static final String DEFAULT_CONFIG_FILE = "config/database.properties";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private static final String SOURCE;

    static {
        Properties props = new Properties();
        String source = "defaults";
        String configured = System.getProperty("soktrapa.config");
        Path file = Paths.get(configured != null ? configured : DEFAULT_CONFIG_FILE);
        if (Files.isRegularFile(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
                source = file.toAbsolutePath().toString();
            } catch (IOException e) {
                System.err.println("[SOKTRAPA] Unable to read " + file + ": " + e.getMessage());
            }
        }

        URL = firstNonNull(System.getenv("SOKTRAPA_DB_URL"), props.getProperty("db.url"), DEFAULT_URL);
        USER = firstNonNull(System.getenv("SOKTRAPA_DB_USER"), props.getProperty("db.user"), null);
        PASSWORD = firstNonNull(System.getenv("SOKTRAPA_DB_PASSWORD"), props.getProperty("db.password"), "");
        SOURCE = (System.getenv("SOKTRAPA_DB_USER") != null) ? "environment variables" : source;
    }

    private DatabaseConfig() {
    }

    private static String firstNonNull(String a, String b, String fallback) {
        if (a != null) return a;
        if (b != null) return b.trim();
        return fallback;
    }

    /** Loads the MySQL JDBC driver; returns false if the connector JAR is missing. */
    public static boolean loadDriver() {
        try {
            Class.forName(DRIVER_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Opens a new JDBC connection. Callers are responsible for closing it,
     * ideally with try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        if (USER == null || USER.isEmpty()) {
            throw new SQLException(
                "Database user is not configured. Copy config/database.properties.example to "
                + "config/database.properties (or set SOKTRAPA_DB_USER / SOKTRAPA_DB_PASSWORD).");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** JDBC URL in use (never includes the password). */
    public static String getUrl() {
        return URL;
    }

    /** Where the configuration was loaded from, for diagnostics. */
    public static String getSource() {
        return SOURCE;
    }
}

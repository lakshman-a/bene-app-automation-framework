package qa.beneapp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import qa.beneapp.config.ConfigManager;

/**
 * DBUtil - minimal JDBC helper for verifying bene-app data in PostgreSQL.
 *
 * Connection details (url / user / password / schema) come from the active
 * config-&lt;env&gt;.properties. The schema is applied via the connection's
 * search_path so queries can use unqualified table names.
 *
 * Example:
 *   List&lt;Map&lt;String,Object&gt;&gt; rows =
 *       DBUtil.query("SELECT * FROM accounts WHERE account_number = '401K-30001'");
 */
public final class DBUtil {

    private DBUtil() {
    }

    private static Connection connect() throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(
                ConfigManager.get("db.url"),
                ConfigManager.get("db.user"),
                ConfigManager.get("db.password"));
        String schema = ConfigManager.get("db.schema");
        if (schema != null && !schema.isBlank()) {
            try (Statement s = conn.createStatement()) {
                s.execute("SET search_path TO " + schema);
            }
        }
        return conn;
    }

    /** Run any SELECT and return rows as a List of column-name -&gt; value maps. */
    public static List<Map<String, Object>> query(String sql) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    /** Run an INSERT/UPDATE/DELETE and return the affected row count. */
    public static int update(String sql) throws Exception {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }
}

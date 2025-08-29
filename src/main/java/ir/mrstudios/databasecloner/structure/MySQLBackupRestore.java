package ir.mrstudios.databasecloner.structure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MySQLBackupRestore {

    private final DataSource dataSource;
    private final ExecutorService executor;
    private final Lock fileLock = new ReentrantLock();

    public MySQLBackupRestore(String host, int port, String database, String user, String password, int threadPoolSize) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(threadPoolSize + 2);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        this.dataSource = new HikariDataSource(config);
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void backup(String filePath) throws SQLException, IOException {
        final List<String> tables = getTables();

        if (tables.isEmpty()) {
            System.out.println("⚠️ Warning: No tables found in the database.");
            return;
        }

        System.out.println("📋 Found " + tables.size() + " tables to backup.");

        final List<Future<TableBackupResult>> futures = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(tables.size());

        for (final String table : tables) {
            final Future<TableBackupResult> future = this.executor.submit(() -> {
                try {
                    return processTable(table);
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        try (final FileWriter writer = new FileWriter(filePath, false)) {
            writer.write("-- MR MySQL Database Backup\n");
            writer.write("-- Generated on: " + new java.util.Date() + "\n");
            writer.write("-- Total tables: " + tables.size() + "\n\n");
            writer.write("SET FOREIGN_KEY_CHECKS=0;\n");
            writer.write("SET SQL_MODE = \"NO_AUTO_VALUE_ON_ZERO\";\n");
            writer.write("SET AUTOCOMMIT = 0;\n");
            writer.write("START TRANSACTION;\n\n");

            int completedTables = 0;
            for (final Future<TableBackupResult> future : futures) {
                try {
                    final TableBackupResult result = future.get();

                    fileLock.lock();
                    try {
                        writer.write(result.content());
                        writer.flush();
                        completedTables++;
                        System.out.println("✅ Completed backup for table: " + result.tableName() +
                                           " (" + completedTables + "/" + tables.size() + ")");
                    } finally {
                        fileLock.unlock();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("❌ Backup interrupted for a table");
                } catch (ExecutionException e) {
                    System.err.println("❌ Error processing table: " + e.getCause().getMessage());
                }
            }

            writer.write("\nCOMMIT;\n");
            writer.write("SET FOREIGN_KEY_CHECKS=1;\n");
            System.out.println("🎉 Backup completed successfully! File: " + filePath);
        }

        try {
            latch.await(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("⚠️ Warning: Backup process was interrupted");
        }
    }

    private List<String> getTables() throws SQLException {
        final List<String> tables = new ArrayList<>();

        try (final Connection conn = this.dataSource.getConnection();
             final Statement stmt = conn.createStatement();
             final ResultSet rsTables = stmt.executeQuery("SHOW TABLES")) {

            while (rsTables.next()) {
                tables.add(rsTables.getString(1));
            }
        }

        return tables;
    }

    private TableBackupResult processTable(String table) {
        final StringBuilder content = new StringBuilder();

        try (final Connection conn = this.dataSource.getConnection()) {
            this.generateTableStructure(conn, table, content);

            this.generateTableData(conn, table, content);

        } catch (SQLException e) {
            content.append("-- ❌ Error processing table `").append(table).append("`: ")
                    .append(e.getMessage()).append("\n\n");
            System.err.println("❌ Error processing table " + table + ": " + e.getMessage());
        }

        return new TableBackupResult(table, content.toString());
    }

    private void generateTableStructure(Connection conn, String table, StringBuilder content) throws SQLException {
        try (final Statement stmt = conn.createStatement();
             final ResultSet rsCreate = stmt.executeQuery("SHOW CREATE TABLE `" + table + "`")) {

            if (rsCreate.next()) {
                final String createSQL = rsCreate.getString(2);
                content.append("-- ----------------------------\n");
                content.append("-- Table structure for `").append(table).append("`\n");
                content.append("-- ----------------------------\n");
                content.append("DROP TABLE IF EXISTS `").append(table).append("`;\n");
                content.append(createSQL).append(";\n\n");
            }
        }
    }

    private void generateTableData(Connection conn, String table, StringBuilder content) throws SQLException {
        try (final Statement stmt = conn.createStatement();
             final ResultSet rsData = stmt.executeQuery("SELECT * FROM `" + table + "`")) {

            final ResultSetMetaData meta = rsData.getMetaData();
            final int columnCount = meta.getColumnCount();
            int rowCount = 0;

            content.append("-- ----------------------------\n");
            content.append("-- Data for table `").append(table).append("`\n");
            content.append("-- ----------------------------\n");

            if (rsData.next()) {
                content.append("INSERT INTO `").append(table).append("` VALUES \n");

                do {
                    if (rowCount > 0) content.append(",\n");

                    content.append("(");
                    for (int i = 1; i <= columnCount; i++) {
                        final Object value = rsData.getObject(i);

                        if (value == null) {
                            content.append("NULL");
                        } else if (value instanceof String || value instanceof java.util.Date) {
                            String stringValue = value.toString()
                                    .replace("\\", "\\\\")
                                    .replace("'", "\\'")
                                    .replace("\n", "\\n")
                                    .replace("\r", "\\r")
                                    .replace("\t", "\\t");
                            content.append("'").append(stringValue).append("'");
                        } else {
                            content.append(value);
                        }

                        if (i < columnCount) content.append(", ");
                    }
                    content.append(")");
                    rowCount++;

                    if (rowCount % 1000 == 0 && rsData.next()) {
                        content.append(";\n\nINSERT INTO `").append(table).append("` VALUES \n");
                        rsData.previous();
                        rowCount = 0;
                    }

                } while (rsData.next());

                content.append(";\n");
            } else {
                content.append("-- No data found in table `").append(table).append("`\n");
            }

            content.append("\n");
        }
    }

    public void shutdown() {
        try {
            this.executor.shutdown();
            if (!this.executor.awaitTermination(30, TimeUnit.SECONDS)) {
                this.executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (this.dataSource instanceof HikariDataSource) {
            ((HikariDataSource) this.dataSource).close();
            System.out.println("🔒 Connection pool closed successfully.");
        }
    }

    private record TableBackupResult(String tableName, String content) {
    }

    public void testConnection() throws SQLException {
        try (Connection conn = this.dataSource.getConnection()) {
            final DatabaseMetaData meta = conn.getMetaData();
            System.out.println("📊 Database: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            System.out.println("🔗 Connection pool is working properly!");
        }
    }
}
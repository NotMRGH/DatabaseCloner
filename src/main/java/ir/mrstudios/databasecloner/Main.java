package ir.mrstudios.databasecloner;

import ir.mrstudios.databasecloner.cloner.MongoCloner;
import ir.mrstudios.databasecloner.cloner.MySQLCloner;
import ir.mrstudios.databasecloner.enums.Config;
import ir.mrstudios.databasecloner.managers.ConfigManager;
import ir.mrstudios.databasecloner.models.MongoBackupRestore;
import ir.mrstudios.databasecloner.models.MySQLBackupRestore;
import lombok.Getter;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
public class Main {

    @Getter
    private static Main instance;

    private final ConfigManager configManager;

    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("=== Database Cloner ===");
        System.out.println("1️⃣ Run from config");
        System.out.println("2️⃣ Run from command-line");
        System.out.print("👉 Select option (1 or 2): ");

        final int choice = Integer.parseInt(scanner.nextLine());

        if (choice == 1) {
            new Main();
        } else if (choice == 2) {
            console();
        } else {
            System.out.println("❌ Invalid choice. Exiting...");
        }
    }

    public Main() {
        instance = this;
        this.configManager = new ConfigManager();

        if (Config.MONGO_ENABLE.getAs(Boolean.class)) {
            final String uri = Config.MONGO_URI.getAs(String.class);
            final int threads = Config.MONGO_THREADS.getAs(Integer.class);
            final String outputPath = Config.MONGO_PATH.getAs(String.class);
            final int intervalMinutes = Config.MONGO_INTERVAL.getAs(Integer.class);
            final ExecutorService executor = Executors.newFixedThreadPool(threads);

            for (String dbName : this.configManager.getSettingsYaml().getStringList("mongo.database-names")) {
                final MongoBackupRestore backupRestore = new MongoBackupRestore(uri, dbName, executor);


                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                    final String fileName = outputPath.replace(".gz", "")
                                            + "-" + System.currentTimeMillis() + ".gz";

                    try {
                        backupRestore.backup(fileName.replaceAll("%name%", dbName));
                    } catch (Exception e) {
                        System.err.println("❌ Backup failed: " + e.getMessage());
                    }
                }, 0, intervalMinutes, TimeUnit.MINUTES);
            }

            System.out.println(
                    "✅ Auto-backup mongo started. Every " + intervalMinutes + " minutes a backup will be created."
            );
            System.out.println("Press CTRL+C to stop.");
        }

        if (Config.MYSQL_ENABLE.getAs(Boolean.class)) {
            final String host = Config.MYSQL_HOST.getAs(String.class);
            final int port = Config.MYSQL_PORT.getAs(Integer.class);
            final String username = Config.MYSQL_USERNAME.getAs(String.class);
            final String password = Config.MYSQL_PASSWORD.getAs(String.class);
            final int threads = Config.MYSQL_THREADS.getAs(Integer.class);
            final String outputPath = Config.MYSQL_PATH.getAs(String.class);
            final int intervalMinutes = Config.MYSQL_INTERVAL.getAs(Integer.class);
            final ExecutorService executor = Executors.newFixedThreadPool(threads);

            for (String dbName : this.configManager.getSettingsYaml().getStringList("mysql.database-names")) {
                final MySQLBackupRestore backupRestore = new MySQLBackupRestore(
                        host,
                        port,
                        dbName,
                        username,
                        password,
                        threads,
                        executor
                );

                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                    try {
                        final String fileName = outputPath.replace(".sql", "")
                                                + "-" + System.currentTimeMillis() + ".sql";

                        backupRestore.testConnection();
                        backupRestore.backup(fileName.replaceAll("%name%", dbName));
                    } catch (Exception e) {
                        System.err.println("❌ Backup failed: " + e.getMessage());
                    }
                }, 0, intervalMinutes, TimeUnit.MINUTES);
            }

            System.out.println(
                    "✅ Auto-backup mysql started. Every " + intervalMinutes + " minutes a backup will be created."
            );
            System.out.println("Press CTRL+C to stop.");
        }
    }

    private static void console() {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("=== Database Cloner ===");
        System.out.println("1️⃣ MongoDB Database");
        System.out.println("2️⃣ MySQL Database");
        System.out.print("👉 Select option (1 or 2): ");

        final int choice = Integer.parseInt(scanner.nextLine());

        if (choice == 1) {
            new MongoCloner();
        } else if (choice == 2) {
            new MySQLCloner();
        } else {
            System.out.println("❌ Invalid choice. Exiting...");
        }
    }

}

package ir.mrstudios.databasecloner;

import ir.mrstudios.databasecloner.cloner.MongoCloner;
import ir.mrstudios.databasecloner.cloner.MySQLCloner;
import ir.mrstudios.databasecloner.enums.Config;
import ir.mrstudios.databasecloner.managers.ConfigManager;
import ir.mrstudios.databasecloner.managers.TelegramManager;
import ir.mrstudios.databasecloner.models.MongoBackupRestore;
import ir.mrstudios.databasecloner.models.MySQLBackupRestore;
import ir.mrstudios.databasecloner.utils.StringUtil;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
public class Main {

    @Getter
    private static Main instance;

    private final ConfigManager configManager;
    private final TelegramManager telegramManager;

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
        this.telegramManager = new TelegramManager();

        if (Config.SQLITE_ENABLE.getAs(Boolean.class)) {
            final int intervalMinutes = Config.SQLITE_INTERVAL.getAs(Integer.class);
            final String outputPath = Config.SQLITE_PATH.getAs(String.class);

            for (String path : this.configManager.getSettingsYaml().getStringList("sqlite.from-paths")) {

                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                    final File sourceFile = new File(path);
                    if (!sourceFile.exists()) {
                        System.out.println("❌ File does not exist.");
                        return;
                    }

                    final String[] split = sourceFile.getName().split("\\.");
                    final String fileExtension = split[split.length - 1];
                    final String fileName = outputPath + File.separator + sourceFile.getName()
                            .replace("." + fileExtension,
                                    "-" + StringUtil.timeNow() + "." + fileExtension
                            );

                    final File copiedFile = new File(fileName);
                    copiedFile.getParentFile().mkdirs();

                    try {
                        Files.copy(sourceFile.toPath(), copiedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println("❌ Backup failed: " + e.getMessage());
                    }

                    this.telegramManager.sendBackup(copiedFile);
                }, 0, intervalMinutes, TimeUnit.MINUTES);
            }
        }

        if (Config.MONGO_ENABLE.getAs(Boolean.class)) {
            final String uri = Config.MONGO_URI.getAs(String.class);
            final int threads = Config.MONGO_THREADS.getAs(Integer.class);
            final String outputPath = Config.MONGO_PATH.getAs(String.class);
            final int intervalMinutes = Config.MONGO_INTERVAL.getAs(Integer.class);
            final ExecutorService executor = Executors.newFixedThreadPool(threads);

            for (String dbName : this.configManager.getSettingsYaml().getStringList("mongo.database-names")) {
                final MongoBackupRestore backupRestore = new MongoBackupRestore(uri, dbName, executor);

                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                    final String fileName = outputPath.replace("%name%", dbName)
                                                    .replace(".gz", "")
                                            + "-" + StringUtil.timeNow() + ".gz";
                    try {

                        backupRestore.backup(fileName);
                    } catch (Exception e) {
                        System.err.println("❌ Backup failed: " + e.getMessage());
                    }

                    this.telegramManager.sendBackup(new File(fileName));
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
                    final String fileName = outputPath.replace("%name%", dbName)
                                                    .replace(".sql", "")
                                            + "-" + StringUtil.timeNow() + ".sql";
                    try {

                        backupRestore.testConnection();
                        backupRestore.backup(fileName);
                    } catch (Exception e) {
                        System.err.println("❌ Backup failed: " + e.getMessage());
                    }

                    this.telegramManager.sendBackup(new File(fileName));
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

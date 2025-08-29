package ir.mrstudios.databasecloner.cloner;

import ir.mrstudios.databasecloner.models.MySQLBackupRestore;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MySQLCloner {

    public MySQLCloner() {
        this.backup();
    }

    private void backup() {
        final Scanner scanner = new Scanner(System.in);

        System.out.print("🔗 Enter MySQL Host (e.g. localhost): ");
        final String host = scanner.nextLine();

        System.out.print("🔢 Enter MySQL Port (default 3306): ");
        final String portInput = scanner.nextLine();
        final int port = portInput.isEmpty() ? 3306 : Integer.parseInt(portInput);

        System.out.print("📂 Enter Database Name: ");
        final String dbName = scanner.nextLine();

        System.out.print("👤 Enter MySQL Username: ");
        final String user = scanner.nextLine();

        System.out.print("🔑 Enter MySQL Password: ");
        final String pass = scanner.nextLine();

        System.out.print("⚙️ Enter Thread Pool Size: ");
        final int threads = Integer.parseInt(scanner.nextLine());

        System.out.print("💾 Enter Backup Output File Path (e.g. backup.sql): ");
        final String outputPath = scanner.nextLine();

        System.out.print("⏰ Enter interval in minutes for backup (enter 0 for one-time backup): ");
        final int intervalMinutes = Integer.parseInt(scanner.nextLine());

        final ExecutorService executor = Executors.newFixedThreadPool(threads);

        final MySQLBackupRestore backupRestore = new MySQLBackupRestore(
                host,
                port,
                dbName,
                user,
                pass,
                threads,
                executor
        );

        if (intervalMinutes > 0) {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                try {
                    final String fileName = outputPath.replace(".sql", "")
                                            + "-" + System.currentTimeMillis() + ".sql";

                    backupRestore.testConnection();
                    backupRestore.backup(fileName);
                } catch (Exception e) {
                    System.err.println("❌ Backup failed: " + e.getMessage());
                }
            }, 0, intervalMinutes, TimeUnit.MINUTES);
            System.out.println(
                    "✅ Auto-backup started. Every " + intervalMinutes + " minutes a backup will be created."
            );
            System.out.println("Press CTRL+C to stop.");
            return;
        }

        try {
            final String fileName = outputPath.replace(".sql", "")
                                    + "-" + System.currentTimeMillis() + ".sql";

            backupRestore.testConnection();
            backupRestore.backup(fileName);
        } catch (Exception e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
        } finally {
            backupRestore.shutdown();
        }
    }
}

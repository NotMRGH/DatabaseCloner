package ir.mrstudios.databasecloner.cloner;

import ir.mrstudios.databasecloner.structure.MongoBackupRestore;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MongoCloner {

    public MongoCloner() {
        this.start();
    }

    private void start() {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("=== Database Cloner ===");
        System.out.println("1️⃣ Backup Database");
        System.out.println("2️⃣ Restore Database");
        System.out.print("👉 Select option (1 or 2): ");

        final int choice = Integer.parseInt(scanner.nextLine());

        if (choice == 1) {
            this.backup();
        } else if (choice == 2) {
            this.restore();
        } else {
            System.out.println("❌ Invalid choice. Exiting...");
        }
    }

    private void restore() {
        final Scanner scanner = new Scanner(System.in);

        System.out.print("🔗 Enter MongoDB Connection URI: ");
        final String uri = scanner.nextLine();

        System.out.print("📂 Enter Database Name: ");
        final String dbName = scanner.nextLine();

        System.out.print("⚙️ Enter Thread Pool Size: ");
        final int threads = Integer.parseInt(scanner.nextLine());

        System.out.print("💾 Enter Backup File Path (to restore from, e.g. backup.gz): ");
        final String backupPath = scanner.nextLine();

        System.out.print("🗑️ Drop existing collections before restore? (yes/no): ");
        final boolean dropExisting = scanner.nextLine().trim().equalsIgnoreCase("yes");

        final MongoBackupRestore backupRestore = new MongoBackupRestore(uri, dbName, threads);

        try {
            backupRestore.restore(backupPath, dropExisting);
            System.out.println("✅ Restore completed successfully!");
        } catch (Exception e) {
            System.err.println("❌ Restore failed: " + e.getMessage());
        } finally {
            backupRestore.shutdown();
        }
    }

    private void backup() {
        final Scanner scanner = new Scanner(System.in);

        System.out.print("🔗 Enter MongoDB Connection URI: ");
        final String uri = scanner.nextLine();

        System.out.print("📂 Enter Database Name: ");
        final String dbName = scanner.nextLine();

        System.out.print("⚙️ Enter Thread Pool Size: ");
        final int threads = Integer.parseInt(scanner.nextLine());

        System.out.print("💾 Enter Backup Output File Path (e.g. backup.gz): ");
        final String outputPath = scanner.nextLine();

        System.out.print("⏰ Enter interval in minutes for backup (enter 0 for one-time backup): ");
        final int intervalMinutes = Integer.parseInt(scanner.nextLine());

        final MongoBackupRestore backupRestore = new MongoBackupRestore(uri, dbName, threads);

        if (intervalMinutes > 0) {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                try {
                    backupRestore.backup(outputPath);
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
            backupRestore.backup(outputPath);
        } catch (Exception e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
        } finally {
            backupRestore.shutdown();
        }
    }
}

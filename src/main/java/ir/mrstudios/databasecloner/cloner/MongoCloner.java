package ir.mrstudios.databasecloner.cloner;

import ir.mrstudios.databasecloner.models.MongoBackupRestore;
import ir.mrstudios.databasecloner.utils.StringUtil;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MongoCloner {

    public MongoCloner() {
        this.start();
    }

    private void start() {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("=== Mongo Database Cloner ===");
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

        final ExecutorService executor = Executors.newFixedThreadPool(threads);

        final MongoBackupRestore backupRestore = new MongoBackupRestore(uri, dbName, executor);

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

        final ExecutorService executor = Executors.newFixedThreadPool(threads);

        final MongoBackupRestore backupRestore = new MongoBackupRestore(uri, dbName, executor);

        try {
            final String fileName = outputPath.replace(".gz", "")
                                    + "-" + StringUtil.timeNow() + ".gz";

            backupRestore.backup(fileName);
        } catch (Exception e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
        } finally {
            backupRestore.shutdown();
        }
    }
}

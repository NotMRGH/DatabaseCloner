package ir.mrstudios.databasecloner;

import ir.mrstudios.databasecloner.cloner.MongoCloner;
import ir.mrstudios.databasecloner.cloner.MySQLCloner;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
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

# Database Cloner

A Java-based database backup and restore utility that supports both MongoDB and MySQL. Easily create scheduled backups and restore databases with multi-threading support.

## Features

- 🚀 **Easy-to-use** command-line interface
- ⚡ **Multi-threaded** backup and restore operations
- ⏰ **Scheduled** automated backups
- 🔄 **Seamless** database restoration
- 🔒 **Secure** handling of database credentials
- 🧩 **Lightweight** and easy to integrate

## Prerequisites

- Java 17 or higher
- MongoDB (local or remote instance) for MongoDB operations
- MySQL Server for MySQL operations
- Maven (for building from source)

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/DatabaseCloner.git
   cd DatabaseCloner
   ```

2. Build the project:
   ```bash
   mvn clean package
   ```

3. Run the application:
   ```bash
   java -jar target/DatabaseCloner-1.0.0.jar
   ```

## Usage

### MongoDB Operations

#### Backup a MongoDB Database

1. Run the application and select `1` for MongoDB Database
2. Choose option `1` for backup
3. Enter your MongoDB connection URI
4. Specify the database name
5. Set the thread pool size (recommended: 4-8 for most systems)
6. Provide the output file path (e.g., `mongo_backup.gz`)
7. Set the backup interval in minutes (0 for one-time backup)

Example:
```
=== Database Cloner ===
1️⃣ MongoDB Database
2️⃣ MySQL Database
👉 Select option (1 or 2): 1

=== MongoDB Operations ===
1️⃣ Backup Database
2️⃣ Restore Database
👉 Select option (1 or 2): 1
🔗 Enter MongoDB Connection URI: mongodb://localhost:27017
📂 Enter Database Name: mydatabase
⚙️ Enter Thread Pool Size: 4
💾 Enter Backup Output File Path (e.g. mongo_backup.gz): mybackup.gz
⏰ Enter interval in minutes for backup (enter 0 for one-time backup): 60
```

#### Restore a MongoDB Database

1. Run the application and select `1` for MongoDB Database
2. Choose option `2` for restore
3. Enter your MongoDB connection URI
4. Specify the target database name
5. Set the thread pool size
6. Provide the backup file path
7. Choose whether to drop existing collections

Example:
```
=== Database Cloner ===
1️⃣ MongoDB Database
2️⃣ MySQL Database
👉 Select option (1 or 2): 1

=== MongoDB Operations ===
1️⃣ Backup Database
2️⃣ Restore Database
👉 Select option (1 or 2): 2
🔗 Enter MongoDB Connection URI: mongodb://localhost:27017
📂 Enter Database Name: mydatabase
⚙️ Enter Thread Pool Size: 4
💾 Enter Backup File Path (to restore from, e.g. mongo_backup.gz): mybackup.gz
🗑️ Drop existing collections before restore? (yes/no): yes
```

### MySQL Operations

#### Backup a MySQL Database

1. Run the application and select `2` for MySQL Database
2. Enter your MySQL host (e.g., localhost)
3. Enter the MySQL port (default: 3306)
4. Enter the database name
5. Enter MySQL username and password
6. Set the thread pool size (recommended: 4-8 for most systems)
7. Provide the output file path (e.g., `mysql_backup.sql`)
8. Set the backup interval in minutes (0 for one-time backup)

Example:
```
=== Database Cloner ===
1️⃣ MongoDB Database
2️⃣ MySQL Database
👉 Select option (1 or 2): 2

=== MySQL Database Backup ===
🔗 Enter MySQL Host (e.g. localhost): localhost
🔢 Enter MySQL Port (default 3306): 3306
📂 Enter Database Name: mydatabase
👤 Enter MySQL Username: root
🔑 Enter MySQL Password: ********
⚙️ Enter Thread Pool Size: 4
💾 Enter Output File Path (e.g. backup.sql): mysql_backup.sql
⏰ Enter interval in minutes for backup (enter 0 for one-time backup): 0
```

## Configuration

The application is configured using command-line inputs. For automated backups, you can create a shell script to run the application with predefined parameters.

## Building from Source

1. Ensure you have Java 17+ and Maven installed
2. Clone the repository
3. Run `mvn clean package`
4. The executable JAR will be available in the `target` directory

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

Built with ❤️ by [NotMR_GH]

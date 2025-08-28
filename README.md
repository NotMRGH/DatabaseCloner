# Database Cloner

A Java-based MongoDB backup and restore utility that allows you to easily create scheduled backups and restore databases with multi-threading support.

## Features

- 🚀 **Easy-to-use** command-line interface
- ⚡ **Multi-threaded** backup and restore operations
- ⏰ **Scheduled** automated backups
- 🔄 **Seamless** database restoration
- 🔒 **Secure** handling of database credentials
- 🧩 **Lightweight** and easy to integrate

## Prerequisites

- Java 17 or higher
- MongoDB (local or remote instance)

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

### Backup a Database

1. Run the application and select option `1` for backup
2. Enter your MongoDB connection URI
3. Specify the database name
4. Set the thread pool size (recommended: 4-8 for most systems)
5. Provide the output file path (e.g., `backup.gz`)
6. Set the backup interval in minutes (0 for one-time backup)

Example:
```
=== Database Cloner ===
1️⃣ Backup Database
2️⃣ Restore Database
👉 Select option (1 or 2): 1
🔗 Enter MongoDB Connection URI: mongodb://localhost:27017
📂 Enter Database Name: mydatabase
⚙️ Enter Thread Pool Size: 4
💾 Enter Backup Output File Path (e.g. backup.gz): mybackup.gz
⏰ Enter interval in minutes for backup: 60
```

### Restore a Database

1. Run the application and select option `2` for restore
2. Enter your MongoDB connection URI
3. Specify the target database name
4. Set the thread pool size
5. Provide the backup file path
6. Choose whether to drop existing collections

Example:
```
=== Database Cloner ===
1️⃣ Backup Database
2️⃣ Restore Database
👉 Select option (1 or 2): 2
🔗 Enter MongoDB Connection URI: mongodb://localhost:27017
📂 Enter Database Name: mydatabase
⚙️ Enter Thread Pool Size: 4
💾 Enter Backup File Path (to restore from, e.g. backup.gz): mybackup.gz
🗑️ Drop existing collections before restore? (yes/no): yes
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

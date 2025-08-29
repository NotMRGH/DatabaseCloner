# Database Cloner

A Java-based database backup and restore utility that supports MongoDB, MySQL, and SQLite. Easily create scheduled backups, restore databases with multi-threading support, and receive backups via Telegram with SOCKS5 proxy support.

## Features

- 🚀 **Easy-to-use** command-line interface
- ⚡ **Multi-threaded** backup and restore operations
- ⏰ **Scheduled** automated backups
- 🔄 **Seamless** database restoration
- 🔒 **Secure** handling of database credentials
- 📱 **Telegram** notifications with backup file delivery
- 🔄 **SOCKS5** proxy support for Telegram
- 🗃️ **SQLite** database support
- 🧩 **Lightweight** and easy to integrate

## Prerequisites

- Java 17 or higher
- MongoDB (local or remote instance) for MongoDB operations
- MySQL Server for MySQL operations
- SQLite
- Maven (for building from source)
- Telegram Bot Token (optional, for Telegram notifications)
- SOCKS5 proxy (optional, for Telegram in restricted networks)

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/NotMRGH/DatabaseCloner.git
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
```

## Configuration

The application can be configured in two ways:
1. **Interactive Command-Line Mode**: Answer prompts when running the application
2. **YAML Configuration File**: Create a `config.yml` file for automated backups

### YAML Configuration

When you first run the application, it will create a `config.yml` file in the same directory if it doesn't exist. You can then modify this file with your preferred settings.

#### Configuration Options

```yaml
# SOCKS5 Proxy Configuration (for Telegram)
socks5:
  enable: false  # Enable/disable SOCKS5 proxy
  host: "127.0.0.1"  # SOCKS5 proxy host
  port: 10808     # SOCKS5 proxy port
  username: ""   # SOCKS5 username (if required)
  password: ""   # SOCKS5 password (if required)

# Telegram Configuration
telegram:
  enable: false      # Enable/disable Telegram notifications
  token: "PUT_YOUR_BOT_TOKEN_HERE"  # Telegram Bot Token
  user-id: 123456789  # Your Telegram User ID

# MongoDB Configuration
mongo:
  enable: false  # Set to true to enable MongoDB backup
  uri: "mongodb://localhost:27017"  # MongoDB connection URI
  database-names: # Database names to back up
    - "MRDatabase"
  threads: 4  # Number of threads to use for operations
  path: "backup.gz"  # Default backup file path
  interval: 60  # Backup interval in minutes

# MySQL Configuration
mysql:
  enable: false  # Set to true to enable MySQL backup
  host: "localhost"  # MySQL host
  port: 3306  # MySQL port
  database-names: # Database names to back up
    - "MRDatabase"
  username: "root"  # MySQL username
  password: ""  # MySQL password
  threads: 4  # Number of threads to use for operations
  path: "backup.sql"  # Default backup file path
  interval: 60  # Backup interval in minutes

# SQLite Configuration
sqlite:
  enable: false  # Set to true to enable SQLite backup
  from-paths:
     - "/etc/lib/sqlite.db"
  path: "/root/backups"  # Path to SQLite database file
  interval: 60   # Backup interval in minutes
```

### Running with Configuration

1. **Interactive Mode**: Run the application without arguments and follow the prompts
   ```bash
   java -jar DatabaseCloner-1.0.0.jar
   ```
   Then select option 2 to run from command-line and follow the prompts

2. **Using Config File**:
   - Configure your `config.yml` file with the desired settings
   - Run the application:
     ```bash
     java -jar DatabaseCloner-1.0.0.jar
     ```
   - Select option 1 to run from config
   - The application will automatically detect and use the `config.yml` file

## Telegram Integration

To receive backups via Telegram:

1. Create a new bot using [@BotFather](https://t.me/botfather) on Telegram
2. Copy your bot token
3. Get your Telegram User ID (you can use [@userinfobot](https://t.me/userinfobot))
4. Enable Telegram in the config file and add your token and user ID
5. (Optional) Configure SOCKS5 proxy if you're in a restricted network

### Security Note

- The configuration file may contain sensitive information like database credentials and Telegram tokens
- Keep the `config.yml` file secure and never commit it to version control
- The `.gitignore` file already includes `config.yml` to prevent accidental commits
- When using Telegram, ensure your bot is private and only accessible by authorized users
- For SOCKS5 proxy, consider using authentication for additional security

## Building from Source

1. Ensure you have Java 17+ and Maven installed
2. Clone the repository
3. Run `mvn clean package`
4. The executable JAR will be available in the `target` directory

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

Built with ❤️ by [NotMR_GH]

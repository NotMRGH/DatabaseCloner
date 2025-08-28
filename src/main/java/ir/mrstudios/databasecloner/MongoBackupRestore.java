package ir.mrstudios.databasecloner;

import com.mongodb.client.*;
import org.bson.RawBsonDocument;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MongoBackupRestore {
    private final String connectionUri;
    private final String databaseName;
    private final ExecutorService executor;
    private final AtomicInteger processedCollections = new AtomicInteger(0);
    private final AtomicInteger totalDocuments = new AtomicInteger(0);

    private static final byte[] COLLECTION_SEPARATOR = "##COLLECTION##".getBytes();
    private static final byte[] END_COLLECTION = "##END##".getBytes();

    public MongoBackupRestore(String connectionUri, String databaseName, int threadPoolSize) {
        this.connectionUri = connectionUri;
        this.databaseName = databaseName;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void backup(String outputPath) throws Exception {
        System.out.println("\n🔄 Start Database Backup: " + this.databaseName);
        long startTime = System.currentTimeMillis();

        try (MongoClient mongoClient = MongoClients.create(this.connectionUri)) {
            final MongoDatabase database = mongoClient.getDatabase(this.databaseName);

            final List<String> collectionNames = new ArrayList<>();
            database.listCollectionNames().forEach(collectionNames::add);
            System.out.println("📊 Number of collections: " + collectionNames.size());

            final Map<String, ByteArrayOutputStream> collectionData = new ConcurrentHashMap<>();
            final CountDownLatch latch = new CountDownLatch(collectionNames.size());

            for (String collectionName : collectionNames) {
                this.executor.submit(() -> {
                    try {
                        System.out.println("⚙️ Collection processing: " + collectionName);
                        collectionData.put(collectionName, this.backupCollection(database, collectionName));
                        System.out.printf("✅ Collection %s completed (%d/%d)\n",
                                collectionName, this.processedCollections.incrementAndGet(), collectionNames.size());
                    } catch (Exception e) {
                        System.err.println("❌ Error in collection backup " + collectionName + ": " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            if (!latch.await(1, TimeUnit.DAYS)) {
                throw new RuntimeException("Timeout in backup!");
            }

            System.out.println("📦 Compress the final file...");
            this.writeCompressedBackup(outputPath, collectionData);

            final File backupFile = new File(outputPath);
            System.out.print("✨ Backup complete!\n");
            System.out.print("📈 Final statistics:\n");
            System.out.printf("- Collections: %d\n", this.processedCollections.get());
            System.out.printf("- All documents: %d\n", this.totalDocuments.get());
            System.out.printf("- Time: %.2f seconds\n", (System.currentTimeMillis() - startTime) / 1000.0);
            System.out.printf("- File: %s\n", outputPath);
            System.out.printf("- File size: %.2f MB", backupFile.length() / (1024.0 * 1024.0));
        }
    }

    public void restore(String backupPath, boolean dropExisting) throws Exception {
        System.out.println("\n🔄 Start Restore from: " + backupPath);
        final long startTime = System.currentTimeMillis();

        final File backupFile = new File(backupPath);
        if (!backupFile.exists()) {
            throw new FileNotFoundException("Backup file not found: " + backupPath);
        }

        System.out.printf("📂 Backup file size: %.2f MB\n", backupFile.length() / (1024.0 * 1024.0));

        try (MongoClient mongoClient = MongoClients.create(this.connectionUri);
             final FileInputStream fileInputStream = new FileInputStream(backupPath);
             final GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
             final DataInputStream dataInputStream = new DataInputStream(gzipInputStream)) {

            final MongoDatabase database = mongoClient.getDatabase(this.databaseName);

            final String header = dataInputStream.readUTF();
            if (!"MR_MONGO_BACKUP_V1".equals(header)) {
                throw new IOException("The backup file format is invalid!");
            }

            final String backupDbName = dataInputStream.readUTF();
            final long backupTime = dataInputStream.readLong();
            final int collectionCount = dataInputStream.readInt();

            System.out.println("📋 Backup information:");
            System.out.println("- Main database: " + backupDbName);
            System.out.println("- Backup time: " + new Date(backupTime));
            System.out.println("- Number of collections: " + collectionCount);


            final List<RestoreTask> restoreTasks = new ArrayList<>();

            for (int i = 0; i < collectionCount; i++) {
                final byte[] separator = new byte[COLLECTION_SEPARATOR.length];
                dataInputStream.readFully(separator);

                if (!Arrays.equals(separator, COLLECTION_SEPARATOR)) {
                    throw new IOException("Invalid data format!");
                }

                final int dataLength = dataInputStream.readInt();
                final byte[] collectionData = new byte[dataLength];
                dataInputStream.readFully(collectionData);

                final byte[] endMarker = new byte[END_COLLECTION.length];
                dataInputStream.readFully(endMarker);

                if (!Arrays.equals(endMarker, END_COLLECTION)) {
                    throw new IOException("End marker collection not found!");
                }

                restoreTasks.add(new RestoreTask(collectionData));
            }

            final CountDownLatch latch = new CountDownLatch(restoreTasks.size());
            this.processedCollections.set(0);
            this.totalDocuments.set(0);

            for (RestoreTask task : restoreTasks) {
                this.executor.submit(() -> {
                    try {
                        this.restoreCollection(database, task.data, dropExisting);
                        System.out.printf("✅ Restore collection %d/%d completed.\n",
                                this.processedCollections.incrementAndGet(), collectionCount);
                    } catch (Exception e) {
                        System.err.println("❌ Error in restore: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            if (!latch.await(1, TimeUnit.DAYS)) {
                throw new RuntimeException("Timeout در restore!");
            }

            final long duration = System.currentTimeMillis() - startTime;
            System.out.println("✨ Restore completed.!");
            System.out.print("📈 Final statistics:\n");
            System.out.printf("- Restored collections: %d\n", this.processedCollections.get());
            System.out.printf("- All documents: %d\n", this.totalDocuments.get());
            System.out.printf("- Time: %.2f seconds\n", duration / 1000.0);
        }
    }

    public void shutdown() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(60, TimeUnit.SECONDS)) {
                this.executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executor.shutdownNow();
        }
    }

    private ByteArrayOutputStream backupCollection(MongoDatabase database, String collectionName) throws IOException {
        final MongoCollection<RawBsonDocument> collection = database
                .getCollection(collectionName, RawBsonDocument.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final DataOutputStream stream = new DataOutputStream(byteArrayOutputStream);

        stream.writeUTF(collectionName);
        final long count = collection.countDocuments();
        stream.writeLong(count);

        final AtomicInteger docCount = new AtomicInteger(0);

        try (MongoCursor<RawBsonDocument> cursor = collection.find().cursor()) {
            while (cursor.hasNext()) {
                final byte[] bsonBytes = cursor.next().getByteBuffer().array();

                stream.writeInt(bsonBytes.length);
                stream.write(bsonBytes);

                docCount.incrementAndGet();
                this.totalDocuments.incrementAndGet();

                if (docCount.get() % 1000 == 0) {
                    System.out.printf("📄 %s: %d/%d document\n", collectionName, docCount.get(), count);
                }
            }
        }

        stream.flush();
        return byteArrayOutputStream;
    }

    private void writeCompressedBackup(String outputPath, Map<String, ByteArrayOutputStream> collectionData)
            throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath);
             final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fos) {{
                 def.setLevel(9);
             }};
             final DataOutputStream outputStream = new DataOutputStream(gzipOutputStream)) {

            outputStream.writeUTF("MR_MONGO_BACKUP_V1");
            outputStream.writeUTF(this.databaseName);
            outputStream.writeLong(System.currentTimeMillis());
            outputStream.writeInt(collectionData.size());

            for (Map.Entry<String, ByteArrayOutputStream> entry : collectionData.entrySet()) {
                outputStream.write(COLLECTION_SEPARATOR);
                byte[] data = entry.getValue().toByteArray();
                outputStream.writeInt(data.length);
                outputStream.write(data);
                outputStream.write(END_COLLECTION);
            }

            outputStream.flush();
        }
    }

    private void restoreCollection(MongoDatabase database, byte[] data, boolean dropExisting) throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        final DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        final String collectionName = dataInputStream.readUTF();
        final long documentCount = dataInputStream.readLong();

        System.out.printf("⚙️ Restore Collection: %s (%d documents)\n", collectionName, documentCount);

        final MongoCollection<RawBsonDocument> collection = database.getCollection(collectionName, RawBsonDocument.class);

        if (dropExisting) {
            collection.drop();
            System.out.println("🗑️ Previous collection deleted: " + collectionName);
        }

        final List<RawBsonDocument> batch = new ArrayList<>();
        int restored = 0;

        for (long i = 0; i < documentCount; i++) {
            final int docLength = dataInputStream.readInt();
            final byte[] docBytes = new byte[docLength];
            dataInputStream.readFully(docBytes);

            final RawBsonDocument rawBsonDocument = new RawBsonDocument(docBytes);
            batch.add(rawBsonDocument);

            if (batch.size() >= 1000) {
                collection.insertMany(batch);
                restored += batch.size();
                this.totalDocuments.addAndGet(batch.size());
                batch.clear();
                System.out.printf("📄 %s: %d/%d documents restored\n", collectionName, restored, documentCount);
            }
        }

        if (!batch.isEmpty()) {
            collection.insertMany(batch);
            restored += batch.size();
            this.totalDocuments.addAndGet(batch.size());
        }

        System.out.printf("✅ %s: All %d documents restored\n", collectionName, restored);
    }

    private record RestoreTask(byte[] data) {

    }
}
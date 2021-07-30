package io.github.ashisbored.playerpronouns.data;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class BinaryPronounDatabase {
    private final Object2ObjectMap<UUID, String> data;

    private BinaryPronounDatabase(Object2ObjectMap<UUID, String> data) {
        this.data = data;
    }

    private BinaryPronounDatabase() {
        this(new Object2ObjectOpenHashMap<>());
    }

    public void put(UUID uuid, @Nullable String pronouns) {
        if (pronouns == null) {
            this.data.remove(uuid);
        } else {
            this.data.put(uuid, pronouns);
        }
    }

    public @Nullable String get(UUID uuid) {
        return this.data.get(uuid);
    }

    public synchronized void save(Path path) throws IOException {
        try (OutputStream os = Files.newOutputStream(path);
             DataOutputStream out = new DataOutputStream(os)) {

            out.writeShort(0x4567); // some form of magic, idk
            out.writeInt(data.size());

            for (var entry : data.entrySet()) {
                UUID uuid = entry.getKey();
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
                out.writeUTF(entry.getValue());
            }
        }
    }

    public static BinaryPronounDatabase load(Path path) throws IOException {
        if (!Files.exists(path)) {
            return new BinaryPronounDatabase();
        }

        try (InputStream is = Files.newInputStream(path);
             DataInputStream in = new DataInputStream(is)) {

            short magic = in.readShort();
            if (magic != 0x4567) {
                throw new IOException("Invalid DB magic: " + magic);
            }

            int size = in.readInt();
            Object2ObjectOpenHashMap<UUID, String> data = new Object2ObjectOpenHashMap<>();
            for (int i = 0; i < size; i++) {
                long mostSigBits = in.readLong();
                long leastSigBits = in.readLong();
                UUID uuid = new UUID(mostSigBits, leastSigBits);
                String pronouns = in.readUTF();
                String old = data.put(uuid, pronouns);
                if (old != null) {
                    throw new IOException("Duplicate UUID in database: " + uuid);
                }
            }

            return new BinaryPronounDatabase(data);
        }
    }
}

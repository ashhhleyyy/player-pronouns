package dev.ashhhleyyy.playerpronouns.impl.data;

import dev.ashhhleyyy.playerpronouns.api.Pronouns;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class BinaryPronounDatabase {
    private final Object2ObjectMap<UUID, String> data;

    private BinaryPronounDatabase(Object2ObjectMap<UUID, String> data) {
        this.data = data;
    }

    private BinaryPronounDatabase() {
        this(new Object2ObjectOpenHashMap<>());
    }

    public static PalettePronounDatabase convert(Path path) throws IOException {
        Object2ObjectMap<UUID, Pronouns> pronouns = new Object2ObjectOpenHashMap<>();
        Map<String, Component> pronounStrings = PronounList.get().getCalculatedPronounStrings();
        for (var entry : BinaryPronounDatabase.load(path).data.entrySet()) {
            Component formatted = Component.literal(entry.getValue());
            if (pronounStrings.containsKey(entry.getValue())) {
                formatted = pronounStrings.get(entry.getValue());
            }
            pronouns.put(entry.getKey(), new Pronouns(entry.getValue(), formatted, false, null));
        }
        return new PalettePronounDatabase(path, pronouns);
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

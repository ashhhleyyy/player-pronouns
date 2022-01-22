package io.github.ashisbored.playerpronouns.data;

import io.github.ashisbored.playerpronouns.PlayerPronouns;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public interface PronounDatabase {
    void put(UUID player, @Nullable Pronouns pronouns);
    @Nullable Pronouns get(UUID player);
    void save(Path path) throws IOException;

    static PronounDatabase load(Path path) throws IOException {
        if (!Files.exists(path)) {
            // Will create a new empty database.
            return PalettePronounDatabase.load(path);
        }

        boolean legacy = false;
        try (InputStream is = Files.newInputStream(path);
             DataInputStream in = new DataInputStream(is)) {
            short magic = in.readShort();
            if (magic == 0x4567) {
                legacy = true;
            }
        }

        if (legacy) {
            PlayerPronouns.LOGGER.info("Old (1.0.0) format pronoun database found, converting...");
            Path backupPath = path.getParent().resolve(path.getFileName().toString() + ".bak");
            Files.copy(path, backupPath);
            PlayerPronouns.LOGGER.info("Old database backed up to {}", backupPath);
            return BinaryPronounDatabase.convert(path);
        }

        return PalettePronounDatabase.load(path);
    }
}

package dev.ashhhleyyy.playerpronouns.api;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Entrypoint to the API, and provides access to a {@link PronounReader} and {@link ProunounWriter}
 */
public final class PronounsApi {
    private static final List<PronounReader> READERS = new ArrayList<>();
    private static final List<ProunounWriter> WRITERS = new ArrayList<>();

    public static void registerReader(PronounReader reader) {
        READERS.add(reader);
    }

    public static void registerWriter(ProunounWriter writer) {
        WRITERS.add(writer);
    }

    public static Optional<Pronouns> getPronouns(UUID playerId) {
        for (PronounReader reader : READERS) {
            Optional<Pronouns> pronouns = reader.getPronouns(playerId);
            if (pronouns.isPresent()) return pronouns;
        }
        return Optional.empty();
    }

    public static Optional<Pronouns> getPronouns(ServerPlayerEntity player) {
        return getPronouns(player.getUuid());
    }

    public static boolean setPronouns(UUID playerID, @Nullable Pronouns pronouns) {
        for (ProunounWriter writer : WRITERS) {
            boolean result = writer.setPronouns(playerID, pronouns);
            if (result && pronouns != null) return true;
        }
        return pronouns == null;
    }

    public static boolean setPronouns(ServerPlayerEntity player, @Nullable Pronouns pronouns) {
        return setPronouns(player.getUuid(), pronouns);
    }

    /**
     * Allows updating a player's {@link Pronouns}.
     * <p>
     * Methods in this class may invoke blocking IO operations to save the database to disk.
     */
    public interface ProunounWriter {

        boolean setPronouns(UUID playerId, @Nullable Pronouns pronouns);
    }

    /**
     * Allows obtaining a player's {@link Pronouns}
     */
    public interface PronounReader {

        Optional<Pronouns> getPronouns(UUID playerId);
    }
}

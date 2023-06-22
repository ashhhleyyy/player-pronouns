package dev.ashhhleyyy.playerpronouns.api;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Entrypoint to the API, and provides access to a {@link PronounReader} and {@link PronounSetter}
 */
public final class PronounsApi {
    private static @Nullable PronounReader READER = null;
    private static @Nullable PronounSetter SETTER = null;

    /**
     * @return The currently initialised {@link PronounReader}
     */
    public static PronounReader getReader() {
        if (READER == null) {
            throw new IllegalStateException("PronounReader has not been initialised");
        }
        return READER;
    }

    /**
     * @return The currently initialised {@link PronounSetter}
     */
    public static PronounSetter getSetter() {
        if (SETTER == null) {
            throw new IllegalStateException("PronounSetter has not been initialised");
        }
        return SETTER;
    }

    /**
     * Makes the passed reader be set as the default.
     * 
     * This should not be called by most mods, unless they are implementing a custom backend.
     * 
     * @param reader The reader to configure
     */
    public static void initReader(PronounReader reader) {
        if (READER != null) {
            throw new IllegalStateException("PronounReader has already been initialised");
        }
        READER = reader;
    }

    /**
     * Makes the passed setter be set as the default.
     * 
     * This should not be called by most mods, unless they are implementing a custom backend.
     * 
     * @param reader The reader to configure
     */
    public static void initSetter(PronounSetter setter) {
        if (SETTER != null) {
            throw new IllegalStateException("PronounSetter has already been initialised");
        }
        SETTER = setter;
    }

    /**
     * Allows updating a player's {@link Pronouns}.
     * 
     * Methods in this class may invoke blocking IO operations to save the database to disk.
     */
    public interface PronounSetter {
        default boolean setPronouns(ServerPlayerEntity player, @Nullable Pronouns pronouns) {
            return this.setPronouns(player.getUuid(), pronouns);
        }
        boolean setPronouns(UUID playerId, @Nullable Pronouns pronouns);
    }

    /**
     * Allows obtaining a player's {@link Pronouns}
     */
    public interface PronounReader {
        default @Nullable Pronouns getPronouns(ServerPlayerEntity player) {
            return this.getPronouns(player.getUuid());
        }
        @Nullable Pronouns getPronouns(UUID playerId);
    }
}

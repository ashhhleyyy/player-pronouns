package dev.ashhhleyyy.playerpronouns.api;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A provider that can add extra pronouns for a user based on remote data.
 */
public interface ExtraPronounProvider {
    CompletableFuture<Optional<String>> provideExtras(UUID playerId);

    Identifier getId();

    Component getName();

    boolean enabled();
}

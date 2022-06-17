package io.github.ashisbored.playerpronouns;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import io.github.ashisbored.playerpronouns.command.PronounsCommand;
import io.github.ashisbored.playerpronouns.data.PronounDatabase;
import io.github.ashisbored.playerpronouns.data.PronounList;
import io.github.ashisbored.playerpronouns.data.Pronouns;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class PlayerPronouns implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "playerpronouns";

    private static PronounDatabase pronounDatabase;
    public static Config config;

    @Override
    public void onInitialize() {
        LOGGER.info("Player Pronouns initialising...");

        config = Config.load();
        PronounList.load(config);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                Path playerData = server.getSavePath(WorldSavePath.PLAYERDATA);
                if (!Files.exists(playerData)) {
                    Files.createDirectories(playerData);
                }
                pronounDatabase = PronounDatabase.load(playerData.resolve("pronouns.dat"));
            } catch (IOException e) {
                LOGGER.error("Failed to create/load pronoun database!", e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (pronounDatabase != null) {
                try {
                    savePronounDatabase(server);
                } catch (IOException e) {
                    LOGGER.error("Failed to save pronoun database!", e);
                }
            }
        });

        //noinspection CodeBlock2Expr
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> {
            PronounsCommand.register(dispatcher);
        });

        Placeholders.register(new Identifier(MOD_ID, "pronouns"), (ctx, argument)->
                PlayerPronouns.fromContext(ctx, argument, true));

        Placeholders.register(new Identifier(MOD_ID, "raw_pronouns"), (ctx, argument)->
                PlayerPronouns.fromContext(ctx, argument, false));
    }

    private static PlaceholderResult fromContext(PlaceholderContext ctx, @Nullable String argument, boolean formatted) {
        if (!ctx.hasPlayer()) {
            return PlaceholderResult.invalid("missing player");
        }
        String defaultMessage = argument != null ? argument : config.getDefaultPlaceholder();
        ServerPlayerEntity player = ctx.player();
        if (pronounDatabase == null) {
            return PlaceholderResult.value(defaultMessage);
        }
        Pronouns pronouns = pronounDatabase.get(player.getUuid());
        if (pronouns == null) {
            return PlaceholderResult.value(defaultMessage);
        }
        if (formatted) {
            return PlaceholderResult.value(pronouns.formatted());
        } else {
            return PlaceholderResult.value(pronouns.raw());
        }
    }

    public static void reloadConfig() {
        config = Config.load();
        PronounList.load(config);
    }

    private static void savePronounDatabase(MinecraftServer server) throws IOException {
        Path playerData = server.getSavePath(WorldSavePath.PLAYERDATA);
        if (!Files.exists(playerData)) {
            Files.createDirectories(playerData);
        }
        pronounDatabase.save(playerData.resolve("pronouns.dat"));
    }

    public static boolean setPronouns(ServerPlayerEntity player, @Nullable Pronouns pronouns) {
        if (pronounDatabase == null) return false;

        pronounDatabase.put(player.getUuid(), pronouns);
        try {
            savePronounDatabase(Objects.requireNonNull(player.getServer()));
        } catch (IOException e) {
            LOGGER.error("Failed to save pronoun database!", e);
        }

        return true;
    }
}

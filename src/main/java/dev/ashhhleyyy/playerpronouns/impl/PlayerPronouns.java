package dev.ashhhleyyy.playerpronouns.impl;

import dev.ashhhleyyy.playerpronouns.api.Pronouns;
import dev.ashhhleyyy.playerpronouns.api.PronounsApi;
import dev.ashhhleyyy.playerpronouns.impl.command.PronounsCommand;
import dev.ashhhleyyy.playerpronouns.impl.data.PalettePronounDatabase;
import dev.ashhhleyyy.playerpronouns.impl.data.PronounDatabase;
import dev.ashhhleyyy.playerpronouns.impl.data.PronounList;
import dev.ashhhleyyy.playerpronouns.impl.interop.PronounDbClient;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.Calendar.getInstance;

public class PlayerPronouns implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(PlayerPronouns.class);
    public static final String MOD_ID = "playerpronouns";
    public static final String USER_AGENT = "player-pronouns/1.0 (+https://ashhhleyyy.dev/projects/2021/player-pronouns)";
    public static final byte[][] OWOS = new byte[][]{new byte[]{73,110,106,101,99,116,105,110,103,32,119,111,107,101,46,46,46},new byte[]{85,112,103,114,97,100,105,110,103,32,97,109,97,116,101,117,114,32,110,111,117,110,115,46,46,46},new byte[]{80,114,101,112,97,114,105,110,103,32,65,98,115,116,114,97,99,116,80,114,111,110,111,117,110,80,114,111,118,105,100,101,114,70,97,99,116,111,114,121,46,46,46},new byte[]{84,114,97,110,115,105,110,103,32,103,101,110,100,101,114,115,46,46,46},new byte[]{77,97,107,105,110,103,32,116,104,101,32,102,114,111,103,115,32,103,97,121,46,46,46},new byte[]{70,108,121,105,110,103,32,102,108,97,103,115,46,46,46},new byte[]{76,111,99,97,116,105,110,103,32,66,108,97,104,97,106,46,46,46},new byte[]{72,97,112,112,121,32,112,114,105,100,101,32,109,111,110,116,104,33,33}};
    public static Config config;
    private PronounDatabase pronounDatabase;

    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static void reloadConfig() {
        config = Config.load();
        PronounList.load(config);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[PlayerPronouns] {}", new String(OWOS[getInstance().get(Calendar.MONTH) == Calendar.JUNE ? OWOS.length - 1 : new Random().nextInt(OWOS.length - 1)]));

        config = Config.load();
        PronounList.load(config);

        PronounsCommand command = new PronounsCommand();
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
                command.register(dispatcher)
        );

        if (config.integrations().pronounDB()) {
            PronounDbClient pronounDbClient = new PronounDbClient();
            PronounsApi.registerReader(pronounDbClient);
        }

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                Path playerData = server.getSavePath(WorldSavePath.PLAYERDATA);
                if (!Files.exists(playerData)) {
                    Files.createDirectories(playerData);
                }
                pronounDatabase = PalettePronounDatabase.load(playerData.resolve("pronouns.dat"));
                PronounsApi.registerReader(pronounDatabase);
                PronounsApi.registerWriter(pronounDatabase);
            } catch (Throwable e) {
                LOGGER.error("Failed to create/load pronoun database!", e);
            }
        });

        Placeholders.register(PlayerPronouns.identifier("pronouns"), (ctx, argument) ->
                fromContext(ctx, argument, true));

        Placeholders.register(PlayerPronouns.identifier("raw_pronouns"), (ctx, argument) ->
                fromContext(ctx, argument, false));

    }

    private PlaceholderResult fromContext(PlaceholderContext ctx, @Nullable String argument, boolean formatted) {
        if (!ctx.hasPlayer()) {
            return PlaceholderResult.invalid("missing player");
        }
        String defaultMessage = argument != null ? argument : config.getDefaultPlaceholder();
        ServerPlayerEntity player = ctx.player();
        assert player != null;
        if (pronounDatabase == null) {
            return PlaceholderResult.value(defaultMessage);
        }
        Optional<Pronouns> pronouns = pronounDatabase.getPronouns(player.getUuid());
        if (pronouns.isEmpty()) {
            return PlaceholderResult.value(defaultMessage);
        }
        if (formatted) {
            return PlaceholderResult.value(pronouns.get().formatted());
        } else {
            return PlaceholderResult.value(pronouns.get().raw());
        }
    }
}

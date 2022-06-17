package io.github.ashisbored.playerpronouns;

import com.google.gson.JsonObject;
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
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.WorldSavePath;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.include.com.google.common.base.Charsets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerPronouns implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(PlayerPronouns.class);
    public static final String MOD_ID = "playerpronouns";
    private static final Map<String, String> PRONOUNDB_ID_MAP = new HashMap<>() {{
        // short pronoun set identifier map from https://pronoundb.org/docs
        put("unspecified", "ask"); // default if unknown
        put("hh", "he/him");
        put("hi", "he/it");
        put("hs", "he/she");
        put("ht", "he/they");
        put("ih", "it/he");
        put("ii", "it/its");
        put("is", "it/she");
        put("it", "it/they");
        put("shh", "she/he");
        put("sh", "she/her");
        put("si", "she/it");
        put("st", "she/they");
        put("th", "they/he");
        put("ti", "they/it");
        put("ts", "they/she");
        put("tt", "they/them");
        put("any", "any");
        put("other", "other");
        put("ask", "ask");
        put("avoid", "avoid");
    }};

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

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if(config.enablePronounDBSync()) {
                String pronounDbUrl = "https://pronoundb.org/api/v1/lookup?platform=minecraft&id=%s".formatted(handler.getPlayer().getUuid());
                try(CloseableHttpClient client = HttpClients.createMinimal();
                    CloseableHttpResponse resp = client.execute(new HttpGet(pronounDbUrl))) {

                    if(resp.getStatusLine().getStatusCode() == 200) {
                        JsonObject json = JsonHelper.deserialize(new String(resp.getEntity().getContent().readAllBytes(), Charsets.UTF_8));
                        String pronouns = PRONOUNDB_ID_MAP.getOrDefault(json.get("pronouns").getAsString(), "ask");
                        setPronouns(handler.getPlayer(), new Pronouns(pronouns, PronounList.get().getCalculatedPronounStrings().get(pronouns)));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
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

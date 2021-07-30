package io.github.ashisbored.playerpronouns;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Config {
    private static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("allow_custom").forGetter(config -> config.allowCustom),
            Codec.STRING.listOf().fieldOf("single").forGetter(config -> config.single),
            Codec.STRING.listOf().fieldOf("pairs").forGetter(config -> config.pairs)
    ).apply(instance, Config::new));

    private final boolean allowCustom;
    private final List<String> single;
    private final List<String> pairs;

    private Config(boolean allowCustom, List<String> single, List<String> pairs) {
        this.allowCustom = allowCustom;
        this.single = single;
        this.pairs = pairs;
    }

    private Config() {
        this(true, Collections.emptyList(), Collections.emptyList());
    }

    public boolean allowCustom() {
        return allowCustom;
    }

    public List<String> getSingle() {
        return single;
    }

    public List<String> getPairs() {
        return pairs;
    }

    public static Config load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("player-pronouns.json");
        if (!Files.exists(path)) {
            Config config = new Config();
            Optional<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, config).result();
            if (result.isPresent()) {
                try {
                    Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(result.get()));
                } catch (IOException e) {
                    PlayerPronouns.LOGGER.warn("Failed to save default config!", e);
                }
            } else {
                PlayerPronouns.LOGGER.warn("Failed to save default config!");
            }
            return new Config();
        } else {
            try {
                String s = Files.readString(path);
                JsonParser parser = new JsonParser();
                JsonElement ele = parser.parse(s);
                return CODEC.decode(JsonOps.INSTANCE, ele).map(Pair::getFirst).result().orElseGet(Config::new);
            } catch (IOException e) {
                PlayerPronouns.LOGGER.warn("Failed to load config!", e);
                return new Config();
            }
        }
    }
}

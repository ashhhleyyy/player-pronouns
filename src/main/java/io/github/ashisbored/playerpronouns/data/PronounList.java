package io.github.ashisbored.playerpronouns.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.ashisbored.playerpronouns.Config;
import io.github.ashisbored.playerpronouns.PlayerPronouns;
import net.minecraft.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class PronounList {
    private static PronounList INSTANCE;

    private final List<String> defaultSingle;
    private final List<String> defaultPairs;
    private final List<String> customSingle;
    private final List<String> customPairs;
    private final List<String> calculatedPronounStrings;

    public PronounList(List<String> defaultSingle, List<String> defaultPairs, List<String> customSingle, List<String> customPairs) {
        this.defaultSingle = defaultSingle;
        this.defaultPairs = defaultPairs;
        this.customSingle = customSingle;
        this.customPairs = customPairs;
        this.calculatedPronounStrings = this.computePossibleCombinations();
    }

    public List<String> getCalculatedPronounStrings() {
        return this.calculatedPronounStrings;
    }

    private List<String> computePossibleCombinations() {
        List<String> ret = new ArrayList<>();
        ret.addAll(this.defaultSingle);
        ret.addAll(this.customSingle);
        List<String> combinedPairs = new ArrayList<>();
        combinedPairs.addAll(this.defaultPairs);
        combinedPairs.addAll(this.customPairs);
        for (int i = 0; i < combinedPairs.size(); i++) {
            for (int j = 0; j < combinedPairs.size(); j++) {
                if (i == j) continue;
                ret.add(combinedPairs.get(i) + "/" + combinedPairs.get(j));
            }
        }
        ret.sort(Comparator.naturalOrder());
        return ret;
    }

    public static void load(Config config) {
        if (INSTANCE != null) {
            throw new IllegalStateException("PronounList has already been loaded!");
        }

        Pair<List<String>, List<String>> defaults = loadDefaults();
        INSTANCE = new PronounList(
                defaults.getLeft(),
                defaults.getRight(),
                config.getSingle(),
                config.getPairs()
        );
    }

    public static PronounList get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("PronounList has not been loaded!");
        }
        return INSTANCE;
    }

    private static Pair<List<String>, List<String>> loadDefaults() {
        try (InputStream is = Objects.requireNonNull(PronounList.class.getResourceAsStream("/default_pronouns.json"));
             InputStreamReader reader = new InputStreamReader(is)) {
            JsonObject ele = new JsonParser().parse(reader).getAsJsonObject();
            JsonArray jsonSingle = ele.getAsJsonArray("single");
            JsonArray jsonPairs = ele.getAsJsonArray("pairs");
            List<String> single = new ArrayList<>();
            List<String> pairs = new ArrayList<>();
            jsonSingle.forEach(e -> single.add(e.getAsString()));
            jsonPairs.forEach(e -> pairs.add(e.getAsString()));
            return new Pair<>(single, pairs);
        } catch (IOException e) {
            PlayerPronouns.LOGGER.error("Failed to load default pronouns!", e);
            return new Pair<>(Collections.emptyList(), Collections.emptyList());
        }
    }
}

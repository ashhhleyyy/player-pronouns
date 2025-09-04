package dev.ashhhleyyy.playerpronouns.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ashhhleyyy.playerpronouns.impl.data.PronounList;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A combined set of {@link Pronoun}s
 *
 * @param raw       The plain text version of this pronoun set
 * @param formatted The styled version of this pronoun set
 * @param provider  The ID of the provider that these pronouns were fetched from
 */
public record Pronouns(
        String raw,
        Text formatted,
        Identifier provider
) {
    public static final Codec<Pronouns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("raw").forGetter(Pronouns::raw),
            TextCodecs.CODEC.fieldOf("formatted").forGetter(Pronouns::formatted),
            Identifier.CODEC.fieldOf("provider").forGetter(Pronouns::provider)
    ).apply(instance, Pronouns::new));

    public static Pronouns fromString(String pronouns, Identifier provider) {
        Text formatted = PronounList.get().getCalculatedPronounStrings().getOrDefault(pronouns, Text.literal(pronouns));
        return new Pronouns(pronouns, formatted, provider);
    }
}

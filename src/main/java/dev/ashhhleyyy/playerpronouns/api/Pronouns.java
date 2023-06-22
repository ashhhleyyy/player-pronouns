package dev.ashhhleyyy.playerpronouns.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;

/**
 * A combined set of {@link Pronoun}s
 * @param raw The plain text version of this pronoun set
 * @param formatted The styled version of this pronoun set
 * @param remote Whether the pronouns were fetched from a remote API
 */
public record Pronouns(
        String raw,
        Text formatted,
        boolean remote
) {
    public static final Codec<Pronouns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("raw").forGetter(Pronouns::raw),
            Codecs.TEXT.fieldOf("formatted").forGetter(Pronouns::formatted),
            Codec.BOOL.optionalFieldOf("remote", false).forGetter(Pronouns::remote)
    ).apply(instance, Pronouns::new));
}

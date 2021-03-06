package io.github.ashisbored.playerpronouns.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import xyz.nucleoid.codecs.MoreCodecs;

public record Pronouns(
        String raw,
        Text formatted,
        boolean remote
) {
    public static final Codec<Pronouns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("raw").forGetter(Pronouns::raw),
            MoreCodecs.TEXT.fieldOf("formatted").forGetter(Pronouns::formatted),
            Codec.BOOL.optionalFieldOf("remote", false).forGetter(Pronouns::remote)
    ).apply(instance, Pronouns::new));
}

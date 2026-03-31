package dev.ashhhleyyy.playerpronouns.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ashhhleyyy.playerpronouns.impl.data.PronounList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

/**
 * A combined set of {@link Pronoun}s
 *
 * @param raw       The plain text version of this pronoun set
 * @param formatted The styled version of this pronoun set
 * @param remote    Whether the pronouns were fetched from a remote API
 * @param provider  The ID of the external provider that these pronouns were fetched from
 */
public record Pronouns(
        String raw,
        Component formatted,
        boolean remote,
        @Nullable ResourceLocation provider
) {
    public static final Codec<Pronouns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("raw").forGetter(Pronouns::raw),
            ComponentSerialization.CODEC.fieldOf("formatted").forGetter(Pronouns::formatted),
            Codec.BOOL.optionalFieldOf("remote", false).forGetter(Pronouns::remote),
            ResourceLocation.CODEC.optionalFieldOf("provider").forGetter(pronouns -> Optional.ofNullable(pronouns.provider()))
    ).apply(instance, Pronouns::new));

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // lol, lmao
    private Pronouns(String raw, Component formatted, boolean remote, Optional<ResourceLocation> provider) {
        this(raw, formatted, remote, provider.orElse(null));
    }

    private Pronouns(String raw, Component formatted) {
        this(raw, formatted, false, (ResourceLocation) null);
    }

    public static Pronouns fromString(String pronouns) {
        return Pronouns.fromString(pronouns, false, null);
    }

    public static Pronouns fromString(String pronouns, boolean remote, @Nullable ResourceLocation provider) {
        Component formatted = PronounList.get().getCalculatedPronounStrings().getOrDefault(pronouns, Component.literal(pronouns));
        return new Pronouns(pronouns, formatted, remote, provider);
    }
}

package dev.ashhhleyyy.playerpronouns.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.ashhhleyyy.playerpronouns.impl.data.PronounList;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class PronounsArgument {

    private PronounsArgument() {
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> pronouns(String name) {
        return Commands.argument(name, StringArgumentType.greedyString())
                .suggests((ctx, builder) -> {
                    String remaining = builder.getRemainingLowerCase();

                    for (String pronouns : PronounList.get().getCalculatedPronounStrings().keySet()) {
                        if (pronouns.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                            builder.suggest(pronouns);
                        }
                    }

                    return builder.buildFuture();
                });
    }
}

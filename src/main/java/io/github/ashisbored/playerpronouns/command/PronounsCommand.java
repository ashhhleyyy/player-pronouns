package io.github.ashisbored.playerpronouns.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import io.github.ashisbored.playerpronouns.PlayerPronouns;
import io.github.ashisbored.playerpronouns.data.PronounList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static io.github.ashisbored.playerpronouns.command.PronounsArgument.pronouns;
import static net.minecraft.server.command.CommandManager.literal;

public class PronounsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("pronouns")
                .then(pronouns("pronouns")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String pronouns = getString(ctx, "pronouns");

                            if (!PlayerPronouns.config.allowCustom() && !PronounList.get().getCalculatedPronounStrings().contains(pronouns)) {
                                ctx.getSource().sendError(new LiteralText("Custom pronouns have been disabled by the server administrator."));
                                return 0;
                            }

                            if (!PlayerPronouns.setPronouns(player, pronouns)) {
                                ctx.getSource().sendError(new LiteralText("Failed to update pronouns, sorry"));
                            } else {
                                ctx.getSource().sendFeedback(new LiteralText("Updated your pronouns to " + pronouns + "!")
                                        .formatted(Formatting.GREEN), false);
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}

package io.github.ashisbored.playerpronouns.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import io.github.ashisbored.playerpronouns.PlayerPronouns;
import io.github.ashisbored.playerpronouns.data.PronounList;
import io.github.ashisbored.playerpronouns.data.Pronouns;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static io.github.ashisbored.playerpronouns.command.PronounsArgument.pronouns;
import static net.minecraft.server.command.CommandManager.literal;

public class PronounsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("pronouns")
                .then(pronouns("pronouns")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String pronounsString = getString(ctx, "pronouns");

                            Map<String, Text> pronounTexts = PronounList.get().getCalculatedPronounStrings();
                            if (!PlayerPronouns.config.allowCustom() && !pronounTexts.containsKey(pronounsString)) {
                                ctx.getSource().sendError(Text.literal("Custom pronouns have been disabled by the server administrator."));
                                return 0;
                            }

                            Pronouns pronouns;
                            if (pronounTexts.containsKey(pronounsString)) {
                                pronouns = new Pronouns(pronounsString, pronounTexts.get(pronounsString), false);
                            } else {
                                pronouns = new Pronouns(pronounsString, Text.literal(pronounsString), false);
                            }

                            if (!PlayerPronouns.setPronouns(player, pronouns)) {
                                ctx.getSource().sendError(Text.literal("Failed to update pronouns, sorry"));
                            } else {
                                ctx.getSource().sendFeedback(Text.literal("Updated your pronouns to ")
                                        .append(pronouns.formatted())
                                        .formatted(Formatting.GREEN), false);
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                ).then(literal("reload-config")
                        .requires(ctx -> Permissions.check(ctx, "playerpronouns.reload_config", 4))
                        .executes(ctx -> {
                            PlayerPronouns.reloadConfig();
                            ctx.getSource().sendFeedback(Text.literal("Reloaded the config!").formatted(Formatting.GREEN), true);
                            return Command.SINGLE_SUCCESS;
                        })
                ).then(literal("unset")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            if (!PlayerPronouns.setPronouns(player, null)) {
                                ctx.getSource().sendError(Text.literal("Failed to update pronouns, sorry"));
                            } else {
                                ctx.getSource().sendFeedback(Text.literal("Cleared your pronouns!")
                                        .formatted(Formatting.GREEN), false);
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}

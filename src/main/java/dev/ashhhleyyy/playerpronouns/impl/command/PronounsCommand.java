package dev.ashhhleyyy.playerpronouns.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.ashhhleyyy.playerpronouns.api.Pronouns;
import dev.ashhhleyyy.playerpronouns.api.PronounsApi;
import dev.ashhhleyyy.playerpronouns.impl.PlayerPronouns;
import dev.ashhhleyyy.playerpronouns.impl.data.PronounList;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.Map;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static dev.ashhhleyyy.playerpronouns.impl.command.PronounsArgument.pronouns;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PronounsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("pronouns")
                .then(literal("set")
                        .then(pronouns("pronouns")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String pronounsString = getString(ctx, "pronouns");

                                    Map<String, Component> pronounTexts = PronounList.get().getCalculatedPronounStrings();
                                    boolean isCustom = !pronounTexts.containsKey(pronounsString);
                                    if (isCustom && !PlayerPronouns.config.allowCustom()) {
                                        ctx.getSource().sendFailure(Component.literal("Custom pronouns have been disabled by the server administrator."));
                                        return 0;
                                    }

                                    int maxLength = PlayerPronouns.config.maxPronounLength();
                                    if (isCustom && maxLength > 0 && pronounsString.length() > maxLength) {
                                        ctx.getSource().sendFailure(Component.literal("The server administrator has limited the length of custom pronouns to " + maxLength + "."));
                                        return 0;
                                    }

                                    Pronouns pronouns = Pronouns.fromString(pronounsString);

                                    if (!PronounsApi.getSetter().setPronouns(player, pronouns)) {
                                        ctx.getSource().sendFailure(Component.literal("Failed to update pronouns, sorry"));
                                    } else {
                                        ctx.getSource().sendSuccess(() -> Component.literal("Updated your pronouns to ")
                                                .append(pronouns.formatted())
                                                .withStyle(ChatFormatting.GREEN), false);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                ).then(literal("reload-config")
                        .requires(ctx -> Permissions.check(ctx, "playerpronouns.reload_config", PermissionLevel.OWNERS))
                        .executes(ctx -> {
                            PlayerPronouns.reloadConfig();
                            ctx.getSource().sendSuccess(() -> Component.literal("Reloaded the config!").withStyle(ChatFormatting.GREEN), true);
                            return Command.SINGLE_SUCCESS;
                        })
                ).then(literal("unset")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            if (!PronounsApi.getSetter().setPronouns(player, null)) {
                                ctx.getSource().sendFailure(Component.literal("Failed to update pronouns, sorry"));
                            } else {
                                ctx.getSource().sendSuccess(() -> Component.literal("Cleared your pronouns!")
                                        .withStyle(ChatFormatting.GREEN), false);
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                ).then(literal("show")
                        .then(argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    Pronouns pronouns = PronounsApi.getReader().getPronouns(player);
                                    if (pronouns != null) {
                                        ctx.getSource().sendSuccess(() -> Component.literal("")
                                                .append(player.getDisplayName())
                                                .append(Component.literal("'s pronouns are ")
                                                        .append(pronouns.formatted())), false);
                                    } else {
                                        ctx.getSource().sendSuccess(() -> Component.literal("")
                                                .append(player.getDisplayName())
                                                .append(Component.literal(" has not set any pronouns.")), false);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );
    }
}

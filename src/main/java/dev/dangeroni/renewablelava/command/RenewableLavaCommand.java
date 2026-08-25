package dev.dangeroni.renewablelava.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import dev.dangeroni.renewablelava.rule.RenewableLavaRules;
import dev.dangeroni.renewablelava.state.RenewableLavaWorldState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class RenewableLavaCommand {
    private RenewableLavaCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("rl")
                .executes(context -> sendStatus(context.getSource()))
                .then(Commands.literal("status").executes(context -> sendStatus(context.getSource())))
                .then(
                    Commands.literal("enable")
                        .requires(RenewableLavaCommand::canToggleWorldState)
                        .then(
                            Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> setWorldEnabled(context.getSource(), BoolArgumentType.getBool(context, "enabled")))
                        )
                )
        );
    }

    private static int sendStatus(CommandSourceStack source) {
        boolean enabled = RenewableLavaRules.isLavaSourceConversionEnabled(source.getLevel());
        source.sendSuccess(() -> statusMessage(enabled), false);
        return enabled ? 1 : 0;
    }

    private static int setWorldEnabled(CommandSourceStack source, boolean enabled) {
        RenewableLavaWorldState state = RenewableLavaWorldState.get(source.getServer());
        state.setEnabled(enabled);
        source.sendSuccess(() -> statusMessage(RenewableLavaRules.isLavaSourceConversionEnabled(source.getLevel())), false);
        return enabled ? 1 : 0;
    }

    private static boolean canToggleWorldState(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player != null && source.getServer().isSingleplayerOwner(player.nameAndId())) {
            return true;
        }

        return source.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
    }

    private static Component statusMessage(boolean enabled) {
        return Component.literal("Renewable Lava: " + (enabled ? "enabled" : "disabled"));
    }
}

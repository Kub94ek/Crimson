@file:Suppress("UnstableApiUsage")

package me.kub94ek.crimson.command.crimson

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.kub94ek.crimson.command.Command
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class CrimsonHelpCommand : Command {
    private val helpMessages = mapOf(
        "help" to Component.text(
            "/crimson help - ",
            NamedTextColor.GOLD
        ).append(
            Component.translatable(
                "commands.crimson.help.help",
                "Shows this help message",
                NamedTextColor.YELLOW
            )
        ),
        "give" to Component.text(
            "/crimson give <player> <item> - ",
            NamedTextColor.GOLD
        ).append(
            Component.translatable(
                "commands.crimson.help.give",
                "Gives an item to a player",
                NamedTextColor.YELLOW
            )
        ),
        "banhammer" to Component.text(
            "/crimson banhammer <command> - ",
            NamedTextColor.GOLD
        ).append(
            Component.translatable(
                "commands.crimson.help.banhammer",
                "Gives you a ban hammer that executes the set command when you kill a player. " +
                        "Use %p% for the killed player's name.",
                NamedTextColor.YELLOW
            )
        ),
        "whitelist" to Component.text(
            "/crimson whitelist [add <player>|remove <player>|list|clear|on|off|status|reload] - ",
            NamedTextColor.GOLD
        ).append(
            Component.translatable(
                "commands.crimson.help.whitelist",
                "Manages the whitelist of players who can join the server",
                NamedTextColor.YELLOW
            )
        )
    )

    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("help")
            .executes { ctx ->
                var message = Component.text("Crimson Help:").color(NamedTextColor.GOLD)

                helpMessages.forEach { (subcommand, helpMessage) ->
                    if (ctx.source.sender.hasPermission("crimson.$subcommand")) {
                        message = message.append(Component.newline().append(helpMessage))
                    }
                }

                ctx.source.sender.sendMessage(message)

                SINGLE_SUCCESS
            }
            .then(
                Commands.argument("subcommand", StringArgumentType.word())
                    .executes {
                        ctx ->
                        val subcommand = StringArgumentType.getString(ctx, "subcommand")

                        val helpMessage = helpMessages[subcommand]
                        if (helpMessage == null) {
                            ctx.source.sender.sendMessage(
                                Component.translatable(
                                    "commands.crimson.help.unknown_subcommand",
                                    "Unknown subcommand: %s"
                                ).arguments(Component.text(subcommand))
                            )
                            return@executes SINGLE_SUCCESS
                        }

                        ctx.source.sender.sendMessage(helpMessage)
                        SINGLE_SUCCESS
                    }
            )
    }
}
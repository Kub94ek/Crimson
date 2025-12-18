package me.kub94ek.crimson.command.crimson

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import me.kub94ek.crimson.command.Command
import me.kub94ek.kubLib.KubLib
import net.kyori.adventure.key.Key

class CrimsonMenuCommand : Command {
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("menu")
            .requires { it.sender.hasPermission("crimson.menu") }
            .then(
                Commands.argument("player", ArgumentTypes.player())
                    .then(
                        Commands.argument("menu", ArgumentTypes.key())
                            .executes {
                                val menuId = it.getArgument("menu", Key::class.java).asString()
                                val player = it.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                    .resolve(it.source).first()

                                KubLib.getInstance().menuRegistry.openMenu(menuId, player)

                                SINGLE_SUCCESS
                            }
                    )
            )
    }
}
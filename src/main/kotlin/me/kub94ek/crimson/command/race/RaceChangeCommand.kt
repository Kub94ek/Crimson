package me.kub94ek.crimson.command.race

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.kub94ek.crimson.Crimson
import me.kub94ek.crimson.command.Command
import me.kub94ek.crimson.listeners.selectingPlayers
import me.kub94ek.kubLib.KubLib
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class RaceChangeCommand : Command {
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("change")
            .requires { it.sender is Player && it.executor is Player }
            .executes { ctx ->
                val player = ctx.source.executor as? Player ?: return@executes 0
                val economy = Crimson.getInstance().economy!!

                val balance = economy.getBalance(player)
                if (balance < 500000) {
                    player.sendMessage(
                        Component.text(
                            "You don't have enough money! You need at least ❂500,000 to use this command.",
                            NamedTextColor.DARK_RED
                        )
                    )
                    return@executes 1
                }

                val response = economy.withdrawPlayer(player, 500000.0)
                if (!response.transactionSuccess()) return@executes 1

                KubLib.getInstance().menuRegistry.openMenu("crimson:race_select_menu", player)
                selectingPlayers.add(player.uniqueId)

                1
            }
    }
}
package me.kub94ek.crimson.command.race

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.kub94ek.crimson.Crimson
import me.kub94ek.crimson.command.Command
import me.kub94ek.kubLib.KubLib
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class RaceViewCommand : Command {
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("view")
            .requires { it.executor is Player }
            .executes { ctx ->
                val executor = ctx.source.executor as Player
                Bukkit.getScheduler().runTask(Crimson.getInstance(), Runnable {
                    KubLib.getInstance().menuRegistry.openMenu("crimson:view_race", executor)
                })

                return@executes 1
            }
    }
}
package me.kub94ek.crimson.command.race

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import me.kub94ek.crimson.command.Command
import me.kub94ek.crimson.races.getRaceLevel
import me.kub94ek.crimson.races.setRaceLevel

class RaceLevelCommand : Command {
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("level")
            .requires { it.sender.hasPermission("race.level") }
            .then(
                Commands.literal("get")
                    .then(
                        Commands.argument("player", ArgumentTypes.player())
                            .executes { ctx ->
                                val player  = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                            .resolve(ctx.source).first()

                                ctx.source.sender.sendMessage(
                                    "${player.name} has race level ${getRaceLevel(player)}"
                                )

                                1
                            }
                    )
            )
            .then(
                Commands.literal("set")
                    .then(
                        Commands.argument("player", ArgumentTypes.player())
                            .then(
                                Commands.argument("level", IntegerArgumentType.integer(1, 3))
                                    .executes { ctx ->
                                        val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                            .resolve(ctx.source).first()
                                        val level = ctx.getArgument("level", Integer::class.java)

                                        setRaceLevel(player, level.toInt())

                                        ctx.source.sender.sendMessage(
                                            "Set race level of ${player.name} to $level"
                                        )

                                        1
                                    }
                            )
                    )
            )
    }
}
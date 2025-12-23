package me.kub94ek.crimson.command.race

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import me.kub94ek.crimson.command.Command
import me.kub94ek.crimson.races.addRaceXp
import me.kub94ek.crimson.races.getRaceXp
import me.kub94ek.crimson.races.setRaceXp

class RaceXpCommand : Command {
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("xp")
            .requires { it.sender.hasPermission("race.xp") }
            .then(
                Commands.literal("get")
                    .then(
                        Commands.argument("player", ArgumentTypes.player())
                            .executes { ctx ->
                                val player  = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                            .resolve(ctx.source).first()

                                ctx.source.sender.sendMessage(
                                    "${player.name} has race xp ${getRaceXp(player)}"
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
                                Commands.argument("xp", IntegerArgumentType.integer(1, 3))
                                    .executes { ctx ->
                                        val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                            .resolve(ctx.source).first()
                                        val xp = ctx.getArgument("xp", Integer::class.java).toInt()

                                        setRaceXp(player, xp)

                                        ctx.source.sender.sendMessage(
                                            "Set race level of ${player.name} to $xp"
                                        )

                                        1
                                    }
                            )
                    )
            )
            .then(
                Commands.literal("add")
                    .then(
                        Commands.argument("player", ArgumentTypes.player())
                            .then(
                                Commands.argument("xp", IntegerArgumentType.integer(1))
                                    .executes { ctx ->
                                        val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                            .resolve(ctx.source).first()
                                        val xp = ctx.getArgument("xp", Integer::class.java).toInt()

                                        val total = addRaceXp(player, xp)

                                        ctx.source.sender.sendMessage(
                                            "Added $xp to player ${player.name} (total xp: $total)"
                                        )

                                        1
                                    }
                            )
                    )
            )
    }
}
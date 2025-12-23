package me.kub94ek.crimson.command.race

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import me.kub94ek.crimson.command.Command
import me.kub94ek.crimson.command.customRaceTypesSuggestion
import me.kub94ek.crimson.races.Race
import me.kub94ek.crimson.races.getRaceType
import me.kub94ek.crimson.races.setRaceType

class RaceTypeCommand : Command {
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("type")
            .requires { it.sender.hasPermission("race.type") }
            .then(
                Commands.literal("get")
                    .then(
                        Commands.argument("player", ArgumentTypes.player())
                            .executes { ctx ->
                                val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                    .resolve(ctx.source).first()

                                ctx.source.sender.sendMessage("Player ${player.name} has race type ${getRaceType(player)}")
                                1
                            }
                    )
            )
            .then(
                Commands.literal("set")
                    .then(
                        Commands.argument("player", ArgumentTypes.player())
                            .then(
                                Commands.argument("type", StringArgumentType.word())
                                    .suggests(customRaceTypesSuggestion())
                                    .executes { ctx ->
                                        val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                            .resolve(ctx.source).first()
                                        val type = ctx.getArgument("type", String::class.java)

                                        setRaceType(player,
                                            Race.entries.find { it.toString().lowercase() == type } ?: run {
                                                ctx.source.sender.sendMessage("Race type \"$type\" does not exist!")
                                                return@executes 1
                                            }
                                        )

                                        1
                                    }
                            )
                    )
            )
    }
}
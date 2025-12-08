package me.kub94ek.crimson.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

interface Command {
    fun create(): LiteralArgumentBuilder<CommandSourceStack>
}
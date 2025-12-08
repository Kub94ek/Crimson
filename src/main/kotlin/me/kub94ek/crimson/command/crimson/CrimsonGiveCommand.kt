package me.kub94ek.crimson.command.crimson

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import me.kub94ek.crimson.command.Command
import me.kub94ek.crimson.command.customItemsSuggestion
import me.kub94ek.kubLib.KubLib
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CrimsonGiveCommand : Command {
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("give")
            .requires { it.sender.hasPermission("crimson.give") }
            .then(
                Commands.argument("player", ArgumentTypes.players())
                    .then(
                        Commands.argument("item", ArgumentTypes.key())
                            .suggests(customItemsSuggestion())
                            .executes(this::executeOne)
                            .then(
                                Commands.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(this::executeMultiple)
                            )
                    )
            )
    }

    private fun executeOne(ctx: CommandContext<CommandSourceStack>): Int {
        return execute(ctx, 1)
    }

    private fun executeMultiple(ctx: CommandContext<CommandSourceStack>): Int {
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        return execute(ctx, amount)
    }

    @Suppress("SameReturnValue")
    private fun execute(ctx: CommandContext<CommandSourceStack>, amount: Int): Int {
        val itemId = ctx.getArgument("item", Key::class.java).asString()
        val playerResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
        val players = playerResolver.resolve(ctx.source)
        val sender = ctx.source.sender

        val itemRegistry = KubLib.getInstance().itemRegistry
        val item = itemRegistry.getItemStack(itemId)

        if (item == null) {
            sender.sendMessage(
                Component.translatable(
                    "commands.crimson.give.unknown_item",
                    "Unknown item: %s",
                    NamedTextColor.RED
                ).arguments(Component.text(itemId))
            )
            return SINGLE_SUCCESS
        }

        if (players.isEmpty()) {
            sender.sendMessage(
                Component.translatable(
                    "commands.crimson.give.unknown_player",
                    "Unknown player",
                    NamedTextColor.RED
                )
            )
            return SINGLE_SUCCESS
        }

        val items: MutableList<ItemStack> = mutableListOf()
        var leftover = amount
        while (leftover > item.maxStackSize) {
            items.add(item.asQuantity(item.maxStackSize))
            leftover -= item.maxStackSize
        }
        items.add(item.asQuantity(leftover))

        if (players.size == 1) {
            val player = players.first()
            if (sender is Player && player.uniqueId == sender.uniqueId) {
                sender.sendMessage(
                    Component.translatable(
                        "commands.crimson.give.success.self",
                        "You gave yourself %s."
                    ).arguments(
                        Component.text("[")
                            .append(item.effectiveName())
                            .append(Component.text("]"))
                            .hoverEvent(item.asHoverEvent()),
                    )
                )
                player.give(items)
                return SINGLE_SUCCESS
            }

            sender.sendMessage(
                Component.translatable(
                    "commands.crimson.give.success.sender",
                    "You gave %1\$s to %2\$s."
                ).arguments(
                    Component.text("[")
                        .append(item.effectiveName())
                        .append(Component.text("]"))
                        .hoverEvent(item.asHoverEvent()),
                    player.displayName()
                        .hoverEvent(player.asHoverEvent())
                )
            )
            player.give(items)
            return SINGLE_SUCCESS
        }

        players.forEach {
            player ->
            player.give(items)

            player.sendMessage(
                Component.translatable(
                    "commands.crimson.give.success.player",
                    "You have been given %s."
                ).arguments(
                    Component.text("[")
                        .append(item.effectiveName())
                        .append(Component.text("]"))
                        .hoverEvent(item.asHoverEvent()),
                )
            )
        }

        sender.sendMessage(
            Component.translatable(
                "commands.crimson.give.success.multiple",
                "You gave %1\$s to %2\$s players."
            ).arguments(
                Component.text("[")
                    .append(item.effectiveName())
                    .append(Component.text("]"))
                    .hoverEvent(item.asHoverEvent()),
                Component.text("${players.size}"))
        )

        return SINGLE_SUCCESS
    }

}
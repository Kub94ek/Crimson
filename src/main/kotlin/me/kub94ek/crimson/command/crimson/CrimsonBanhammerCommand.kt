@file:Suppress("UnstableApiUsage")

package me.kub94ek.crimson.command.crimson

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.tr7zw.nbtapi.NBT
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import me.kub94ek.crimson.command.Command
import me.kub94ek.kubLib.KubLib
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class CrimsonBanhammerCommand : Command {
    override fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("banhammer")
            .requires { it.sender.hasPermission("crimson.banhammer") }
            .then(
                Commands.argument("command", StringArgumentType.greedyString())
                    .executes {
                        ctx ->
                        val player = ctx.source.executor
                        if (player !is Player) {
                            ctx.source.sender.sendMessage(Component.translatable(
                                "commands.crimson.banhammer.only_players",
                                    "Only players can get a ban hammer!"
                            ))
                            return@executes SINGLE_SUCCESS
                        }

                        var command = StringArgumentType.getString(ctx, "command")
                        if (command.startsWith("/")) {
                            command = command.substring(1)
                        }

                        val banHammer = ItemStack(Material.MACE)
                        val itemMeta = banHammer.itemMeta
                        itemMeta.customName(
                            Component.translatable(
                                "item.crimson.ban_hammer",
                                "Ban Hammer",
                                NamedTextColor.DARK_RED, TextDecoration.BOLD
                            )
                        )
                        itemMeta.lore(listOf(
                            Component.translatable(
                                "item.crimson.ban_hammer.desc",
                                "Command:",
                                NamedTextColor.WHITE
                            ),
                            Component.text(
                                "/$command",
                                NamedTextColor.GRAY
                            )
                        ))
                        itemMeta.addEnchant(
                            RegistryAccess.registryAccess()
                                .getRegistry(RegistryKey.ENCHANTMENT)
                                .get(NamespacedKey.fromString("crimson:ban_hammer")!!)!!,
                            1, false
                        )
                        itemMeta.addAttributeModifier(
                            Attribute.ATTACK_DAMAGE, AttributeModifier(
                                NamespacedKey.fromString("crimson:ban_hammer_damage_bonus")!!,
                                10.0, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlotGroup.MAINHAND
                            )
                        )
                        itemMeta.addItemFlags(
                            ItemFlag.HIDE_ATTRIBUTES,
                            ItemFlag.HIDE_ENCHANTS
                        )
                        banHammer.itemMeta = itemMeta

                        KubLib.getInstance().itemRegistry.getDataCompound(banHammer)
                            ?.setString("item_id", "crimson:ban_hammer")

                        NBT.modify(banHammer) {
                            it.setString("crimson:ban_hammer_command", command)
                        }

                        player.inventory.addItem(banHammer)

                        SINGLE_SUCCESS
                    }
            )
    }
}
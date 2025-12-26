@file:Suppress("UnstableApiUsage")

package me.kub94ek.crimson.listeners

import de.tr7zw.nbtapi.NBT
import me.kub94ek.kubLib.items.ItemRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class BanHammerListener(private val itemRegistry: ItemRegistry) : Listener {

    @EventHandler
    fun onPlayerKill(event: PlayerDeathEvent) {
        val player = event.entity

        val killer = event.damageSource.directEntity ?: return
        if (killer !is Player) return

        val item = killer.inventory.itemInMainHand
        if (item.isEmpty) return
        if (itemRegistry.getItemId(item) != "crimson:ban_hammer") return

        var command: String? = null
        NBT.get(item) {
            command = it.getString("crimson:ban_hammer_command")
        }
        if (command.isNullOrEmpty()) return

        if (command!!.startsWith("tempban") || command!!.startsWith("ban")) {
            if (!command!!.lowercase().contains("x-ray") && !command!!.lowercase().contains("xray")) {
                event.keepInventory = true
                event.drops.clear()
            }
        }

        if (!Bukkit.dispatchCommand(killer, command!!.replace("%p%", player.name))) {
            killer.sendMessage(Component.translatable(
                "commands.crimson.ban_hammer.command_failed",
                "Running the command failed.",
                NamedTextColor.RED
            ))
        }
    }
}
package me.kub94ek.crimson.listeners

import me.kub94ek.crimson.Crimson
import me.kub94ek.kubLib.KubLib
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class EnderArmorListener : Listener {
    private val kubLib = KubLib.getInstance()
    private val itemRegistry = kubLib.itemRegistry
    private val cooldownManager = kubLib.cooldownManager

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return
        val player = event.player
        if (!player.isSneaking) return
        val item = event.item ?: return
        if (!Tag.ITEMS_SWORDS.isTagged(item.type)) return

        val helmet = player.inventory.helmet ?: return
        val chestplate = player.inventory.chestplate ?: return
        val leggings = player.inventory.leggings ?: return
        val boots = player.inventory.boots ?: return

        if (helmet.type.isAir || chestplate.type.isAir
            || leggings.type.isAir || boots.type.isAir) return
        if (itemRegistry.getItemId(helmet) != Crimson.ENDER_HELMET) return
        if (itemRegistry.getItemId(chestplate) != Crimson.ENDER_CHESTPLATE) return
        if (itemRegistry.getItemId(leggings) != Crimson.ENDER_LEGGINGS) return
        if (itemRegistry.getItemId(boots) != Crimson.ENDER_BOOTS) return

        val cooldown = cooldownManager.getRemainingCooldown(player.uniqueId, "crimson:ender_teleport")
        if (cooldown != null && cooldown > 0) {
            player.sendMessage(Component.text("You must wait $cooldown seconds before using this ability.", NamedTextColor.RED))
            return
        }

        val result = player.rayTraceBlocks(8.0) ?: return
        val blockFace = result.hitBlockFace ?: return
        if (blockFace != BlockFace.UP) return
        val block = result.hitBlock ?: return
        player.teleport(Location(player.world, block.x + 0.5, block.y + 1.0, block.z + 0.5, player.yaw, player.pitch))

        cooldownManager.startCooldown(player.uniqueId, "crimson:ender_teleport")

    }
}
package me.kub94ek.crimson.listeners

import me.kub94ek.crimson.Crimson
import me.kub94ek.kubLib.KubLib
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class WitherArmorListener : Listener {
    private val kubLib = KubLib.getInstance()
    private val itemRegistry = kubLib.itemRegistry

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return

        val player = event.entity as? Player ?: return
        val damager = event.damager as? LivingEntity ?: return
        if (event.damage <= 0) return

        if (Random.Default.nextDouble() >= 0.6) return

        val helmet = player.inventory.helmet ?: return
        val chestplate = player.inventory.chestplate ?: return
        val leggings = player.inventory.leggings ?: return
        val boots = player.inventory.boots ?: return

        if (helmet.type.isAir || chestplate.type.isAir
            || leggings.type.isAir || boots.type.isAir) return
        if (itemRegistry.getItemId(helmet) != Crimson.WITHER_HELMET) return
        if (itemRegistry.getItemId(chestplate) != Crimson.WITHER_CHESTPLATE) return
        if (itemRegistry.getItemId(leggings) != Crimson.WITHER_LEGGINGS) return
        if (itemRegistry.getItemId(boots) != Crimson.WITHER_BOOTS) return

        damager.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 60, 255))

    }
}
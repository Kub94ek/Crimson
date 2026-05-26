package me.kub94ek.crimson.listeners

import me.chancesd.pvpmanager.player.CombatPlayer
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class PvpListener : Listener {

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        val hasPvpEnabled = CombatPlayer.get(player)?.hasPvPEnabled() ?: return
        if (hasPvpEnabled) return

        val cause = event.cause

        if (cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
            || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            if (event.damageSource.directEntity?.type == EntityType.CREEPER) return

            event.isCancelled = true
        }
    }
}
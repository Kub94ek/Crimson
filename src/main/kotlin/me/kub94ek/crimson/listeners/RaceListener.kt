package me.kub94ek.crimson.listeners

import me.kub94ek.crimson.Crimson
import me.kub94ek.crimson.races.*
import me.kub94ek.crimson.races.abilities.AttributeAbility
import me.kub94ek.crimson.races.abilities.PotionAbility
import me.kub94ek.kubLib.KubLib
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExhaustionEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

val selectingPlayers = mutableListOf<UUID>()

class RaceListener(private val kubLib: KubLib) : Listener {


    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player

        val race = getRaceType(player)!!
        val level = getRaceLevel(player)

        var removeEffects = false

        if (player.world.name.startsWith("world")) {
            startPotionRunnable(player)
        } else {
            potionRunnables.remove(player.uniqueId)
            removeEffects = true
        }

        race.abilities[level]!!.forEach {
            if (it is AttributeAbility) {
                val attribute = player.getAttribute(it.attribute)!!
                if (attribute.getModifier(it.modifier.key) != null) {
                    attribute.removeModifier(it.modifier.key)
                }
                if (player.world.name.startsWith("world")) {
                    attribute.addModifier(it.modifier)
                }
            } else if (removeEffects && it is PotionAbility) {
                player.removePotionEffect(it.potionEffect.type)
            }
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        val race = getRaceType(player)

        if (race == null) {
            Bukkit.getScheduler().runTaskLater(Crimson.getInstance(), Runnable {
                kubLib.menuRegistry.openMenu("crimson:race_select_menu", player)
                selectingPlayers.add(player.uniqueId)
                player.isInvulnerable = true
            }, 40)
            return
        }

        startPotionRunnable(player)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as Player

        if (!selectingPlayers.contains(player.uniqueId)) return

        if (event.reason == InventoryCloseEvent.Reason.PLAYER) {
            Bukkit.getScheduler().runTask(Crimson.getInstance(), Runnable {
                kubLib.menuRegistry.openMenu(kubLib.menuRegistry.getMenuId(event.inventory.getItem(0)!!)!!, player)
            })
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val type = entity.type
        val killer = entity.killer ?: return

        val level = getRaceLevel(killer)
        if (level >= 3) return

        addRaceXp(killer,
            if (Tag.ENTITY_TYPES_ZOMBIES.isTagged(type)
                || Tag.ENTITY_TYPES_SKELETONS.isTagged(type)
                || Tag.ENTITY_TYPES_ILLAGER.isTagged(type)
                || type == EntityType.ENDERMAN
                || type == EntityType.WITCH
                || type == EntityType.SPIDER
                || type == EntityType.CAVE_SPIDER
                || type == EntityType.CREEPER) {
                10
            } else if (type == EntityType.WARDEN
                || type == EntityType.WITHER
                || type == EntityType.ENDER_DRAGON) {
                250
            } else {
                1
            }
        )
    }


    // ENDER RACE
    @EventHandler
    fun onFoodConsume(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item

        if (item.type != Material.CHORUS_FRUIT) return

        if (getRaceType(player) != Race.ENDER || getRaceLevel(player) == 1) return
        if (event.isCancelled) return

        player.foodLevel += 4
        player.saturation += 4.5f
    }

    // ENDER AND DEMON RACE
    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (event.cause != DamageCause.DROWNING) return

        val race = getRaceType(player)
        if (race != Race.ENDER && race != Race.DEMON) return

        event.damage += 1000
    }

    // DROWNED RACE
    @EventHandler
    fun onExhaustion(event: EntityExhaustionEvent) {
        val player = event.entity as? Player ?: return
        val reason = event.exhaustionReason

        if (reason != EntityExhaustionEvent.ExhaustionReason.SPRINT
            && reason != EntityExhaustionEvent.ExhaustionReason.JUMP_SPRINT
            && reason != EntityExhaustionEvent.ExhaustionReason.JUMP
            && reason != EntityExhaustionEvent.ExhaustionReason.WALK) return

        if (getRaceType(player) != Race.DROWNED) return

        event.exhaustion *= 2
    }
}
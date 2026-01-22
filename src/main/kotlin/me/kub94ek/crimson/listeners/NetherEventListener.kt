package me.kub94ek.crimson.listeners

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class NetherEventListener : Listener {
    private val validSpawnReasons = listOf(
        SpawnReason.NATURAL
    )
    private val spawnableMobs = listOf(
        WitherSkeleton::class.java,
        Blaze::class.java,
        PigZombie::class.java,
        MagmaCube::class.java,
        Zoglin::class.java
    )
    private val spawnableMobTypes = listOf(
        EntityType.WITHER_SKELETON,
        EntityType.BLAZE,
        EntityType.ZOMBIFIED_PIGLIN,
        EntityType.MAGMA_CUBE,
        EntityType.ZOGLIN,
        EntityType.WITHER
    )

    private var counter = 0
    private var currentAmount = 0

    @EventHandler(priority = EventPriority.MONITOR)
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        if (event.location.world.name != "world") return
        if (event.location.distance(
                Location(Bukkit.getWorld("world"), -924.5, 158.0, -1799.5)
        ) > 300) return

        if (!validSpawnReasons.contains(event.spawnReason)) return
        if (event.isCancelled) return

        event.entity.remove()

        if (currentAmount >= 600) return
        if (event.location.y < 86) return
        if (Random.Default.nextDouble() >= 0.99) return

        currentAmount++

        counter++
        if (counter >= 3000) {
            counter = 0

            val wither = event.location.world.spawn(event.location, Wither::class.java, SpawnReason.COMMAND)
            wither.getAttribute(Attribute.MAX_HEALTH)!!.baseValue *= 1.2
            wither.health *= 1.2
            wither.addScoreboardTag("DropsWitherAspect")

            return
        }

        val mob = event.location.world.spawn(event.location, spawnableMobs.random(), SpawnReason.NETHER_PORTAL)
        mob.getAttribute(Attribute.MAX_HEALTH)!!.baseValue *= 1.5
        mob.health *= 1.5

        var attackDamage = mob.getAttribute(Attribute.ATTACK_DAMAGE)
        if (attackDamage == null) {
            mob.registerAttribute(Attribute.ATTACK_DAMAGE)
            attackDamage = mob.getAttribute(Attribute.ATTACK_DAMAGE)
        }

        attackDamage!!.addModifier(AttributeModifier(
            NamespacedKey.fromString("crimson:nether_event")!!,
            0.5, AttributeModifier.Operation.ADD_SCALAR
        ))

        mob.addPotionEffect(PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0, true, false))
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityRemove(event: EntityRemoveEvent) {
        val entity = event.entity as? LivingEntity ?: return
        if (!spawnableMobTypes.contains(entity.type)) return
        if (entity.world.name != "world") return
        if (entity.location.distance(
                Location(Bukkit.getWorld("world"), -924.5, 158.0, -1799.5)
        ) > 300) return

        currentAmount--
    }

}
package me.kub94ek.crimson.races

import me.kub94ek.crimson.Crimson
import me.kub94ek.crimson.races.abilities.AttributeAbility
import me.kub94ek.crimson.races.abilities.PotionAbility
import me.kub94ek.crimson.races.abilities.PotionRunnable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.Style.*
import net.kyori.adventure.text.format.TextDecoration.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID


val potionRunnables = mutableMapOf<UUID, BukkitRunnable>()

fun setRaceType(name: String, type: Race) {
    setRaceType(Bukkit.getPlayer(name) ?: return, type)
}
fun setRaceType(player: Player, type: Race) {
    val crimson = Crimson.getInstance()
    crimson.reloadConfig()
    val name = player.name
    val uuid = player.uniqueId

    val current = getRaceType(name)

    if (!crimson.config.contains("races.$name")) {
        crimson.config.set("races.$name.level", 1)
    }

    val level = getRaceLevel(name)

    crimson.config.set("races.$name.type", type.name)
    crimson.savePluginConfig()

    potionRunnables[uuid]?.cancel()
    potionRunnables.remove(uuid)

    if (current != null) {
        current.abilities[level]!!.forEach {
            if (it is AttributeAbility) {
                val attribute = player.getAttribute(it.attribute)!!
                if (attribute.getModifier(it.modifier.key) != null) {
                    attribute.removeModifier(it.modifier.key)
                }
            } else if (it is PotionAbility) {
                player.removePotionEffect(it.potionEffect.type)
            }
        }
    }

    type.abilities[level]!!.forEach {
        if (it is AttributeAbility) {
            val attribute = player.getAttribute(it.attribute)!!
            if (attribute.getModifier(it.modifier.key) != null) {
                attribute.removeModifier(it.modifier.key)
            }
            attribute.addModifier(it.modifier)
        }
    }

    startPotionRunnable(player)

}

fun getRaceType(player: Player): Race? {
    return getRaceType(player.name)
}
fun getRaceType(name: String): Race? {
    return Race.valueOf(Crimson.getInstance().config.getString("races.$name.type") ?: return null)
}


fun getRaceLevel(player: Player): Int {
    return getRaceLevel(player.name)
}
fun getRaceLevel(name: String): Int {
    return Crimson.getInstance().config.getInt("races.$name.level", 1)
}

fun setRaceLevel(player: Player, level: Int) {
    val crimson = Crimson.getInstance()
    crimson.reloadConfig()
    crimson.config.set("races.${player.name}.level", level)
    crimson.savePluginConfig()

    val type = getRaceType(player) ?: return

    potionRunnables[player.uniqueId]?.cancel()
    potionRunnables.remove(player.uniqueId)

    type.abilities[level-1]!!.forEach {
        if (it is AttributeAbility) {
            val attribute = player.getAttribute(it.attribute)!!
            if (attribute.getModifier(it.modifier.key) != null) {
                attribute.removeModifier(it.modifier.key)
            }
        } else if (it is PotionAbility) {
            player.removePotionEffect(it.potionEffect.type)
        }
    }

    type.abilities[level]!!.forEach {
        if (it is AttributeAbility) {
            val attribute = player.getAttribute(it.attribute)!!
            if (attribute.getModifier(it.modifier.key) != null) {
                attribute.removeModifier(it.modifier.key)
            }
            attribute.addModifier(it.modifier)
        }
    }

    startPotionRunnable(player)
}


fun getRaceXp(player: Player): Int {
    return getRaceXp(player.name)
}
fun getRaceXp(name: String): Int {
    return Crimson.getInstance().config.getInt("races.$name.xp", 0)
}

fun setRaceXp(player: Player, xp: Int) {
    setRaceXp(player.name, xp)
}
fun setRaceXp(name: String, xp: Int) {
    val current = getRaceXp(name)
    if (current == xp) return
    val crimson = Crimson.getInstance()
    crimson.config.set("races.$name.xp", xp)
    crimson.savePluginConfig()

    val currentLevel = getRaceLevel(name)
    if (currentLevel == 3) return
    if (xp >= (1000 + (currentLevel - 1) * 1500)) {
        val player = Bukkit.getPlayer(name) ?: return
        setRaceLevel(player, currentLevel + 1)
        player.sendMessage(
            translatable("message.race.level_up", "You have leveled up to level %s!", GOLD, BOLD)
                .arguments(text((currentLevel+1).toString(), YELLOW, ITALIC))
        )
    }
}

fun addRaceXp(player: Player, xp: Int): Int {
    return addRaceXp(player.name, xp)
}
fun addRaceXp(name: String, xp: Int): Int {
    val currentXp = getRaceXp(name)
    if (xp == 0) {
        return currentXp
    }
    val finalXp = currentXp + xp
    setRaceXp(name, finalXp)
    return finalXp
}


fun startPotionRunnable(player: Player) {
    val effects = mutableListOf<PotionEffect>()

    getRaceType(player)!!.abilities[getRaceLevel(player)]!!.forEach {
        if (it is PotionAbility) {
            effects.add(it.potionEffect)
        }
    }

    val potionRunnable = PotionRunnable(player.uniqueId, effects)
    potionRunnable.runTaskTimer(Crimson.getInstance(), 0, 100)
    potionRunnables[player.uniqueId] = potionRunnable
}


fun createAbilityLore(
    name: Component, description: Component,
    duration: Int?, cooldown: Int?
): List<Component> {
    val lore = mutableListOf(
        translatable("gui.race.ability.base", "Ability: ", style(GOLD, ITALIC.withState(false)))
            .append(name.style(style(YELLOW))),
        description.style(style(GRAY, ITALIC.withState(false)))
    )

    if (duration != null) {
        lore.add(
            translatable("gui.race.ability.duration", "Duration: ", style(DARK_GRAY, ITALIC.withState(false)))
                .append(text("${cooldown}s"))
        )
    }

    if (cooldown != null) {
        lore.add(
            translatable("gui.race.ability.cooldown", "Cooldown: ", style(DARK_GRAY, ITALIC.withState(false)))
                .append(text("${cooldown}s"))
        )
    }

    return lore
}
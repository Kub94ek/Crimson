package me.kub94ek.crimson.races.abilities

import me.kub94ek.crimson.races.potionRunnables
import org.bukkit.Bukkit
import org.bukkit.potion.PotionEffect
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class PotionRunnable(private val playerId: UUID, private val effects: List<PotionEffect>) : BukkitRunnable() {
    override fun run() {
        val player = Bukkit.getPlayer(playerId)
        if (player == null) {
            this.cancel()
            potionRunnables.remove(playerId)
            return
        }

        effects.forEach {
            player.addPotionEffect(it.withDuration(120))
        }
    }
}
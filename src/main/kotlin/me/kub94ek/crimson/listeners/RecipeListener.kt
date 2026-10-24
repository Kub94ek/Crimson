package me.kub94ek.crimson.listeners

import me.kub94ek.crimson.Crimson
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class RecipeListener : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        player.discoverRecipes(
            Crimson.getInstance().recipesToUnlock.map { NamespacedKey.fromString(it)!! }
        )
    }
}
package me.kub94ek.crimson.races

import me.kub94ek.crimson.races.abilities.AttributeAbility
import me.kub94ek.crimson.races.abilities.PotionAbility
import me.kub94ek.crimson.races.abilities.RaceAbility
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey.fromString
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import javax.inject.Provider

enum class Race(
    val displayName: Component,
    val abilityLores: Map<Int, List<Component>>,
    val abilities: Map<Int, List<RaceAbility>>,
    val spawnPos: Provider<Location>
) {
    HUMAN(
        translatable("menu.crimson.race_select.human", "Human", style(WHITE, ITALIC.withState(false))),
        mapOf(
            1 to listOf(text("No buffs, no debuffs", style(GRAY, ITALIC.withState(false)))),
            2 to listOf(text("No buffs, no debuffs", style(GRAY, ITALIC.withState(false)))),
            3 to listOf(text("No buffs, no debuffs", style(GRAY, ITALIC.withState(false))))
        ),
        mapOf(1 to listOf(), 2 to listOf(), 3 to listOf()),
        { Location(Bukkit.getWorld("world"), -69.0, 98.0, -16.0) }
    ),
    GOLEM(
        translatable("menu.crimson.race_select.golem", "Golem", style(WHITE, ITALIC.withState(false))),
        mapOf(
            1 to listOf(
                text("+5 ❤", style(GREEN, ITALIC.withState(false))),
                text("Hero of the Village I", style(GREEN, ITALIC.withState(false))),
                text("Strength I", style(GREEN, ITALIC.withState(false))),
                text("Slowness I", style(RED, ITALIC.withState(false))),
                text("3 blocks tall", style(RED, ITALIC.withState(false)))
            ),
            2 to listOf(
                text("+10 ❤", style(GREEN, ITALIC.withState(false))),
                text("Hero of the Village I", style(GREEN, ITALIC.withState(false))),
                text("Strength II", style(GREEN, ITALIC.withState(false))),
                text("Slowness II", style(RED, ITALIC.withState(false))),
                text("3 blocks tall", style(RED, ITALIC.withState(false)))
            ),
            3 to listOf(
                text("+10 ❤", style(GREEN, ITALIC.withState(false))),
                text("Hero of the Village I", style(GREEN, ITALIC.withState(false))),
                text("Strength II", style(GREEN, ITALIC.withState(false))),
                text("Slowness II", style(RED, ITALIC.withState(false))),
                text("3 blocks tall", style(RED, ITALIC.withState(false)))
            )
        ),
        mapOf(
            1 to listOf(
                AttributeAbility(Attribute.SCALE,
                    AttributeModifier(fromString("crimson:golem_size")!!,
                        0.6, AttributeModifier.Operation.ADD_SCALAR)),
                PotionAbility(PotionEffect(PotionEffectType.STRENGTH, 1, 0, true, false)),
                PotionAbility(PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 1, 0, true, false)),
                PotionAbility(PotionEffect(PotionEffectType.SLOWNESS, 1, 0, true, false)),
                AttributeAbility(Attribute.MAX_HEALTH,
                    AttributeModifier(fromString("crimson:golem_health")!!,
                        0.5, AttributeModifier.Operation.ADD_SCALAR))
            ),
            2 to listOf(
                AttributeAbility(Attribute.SCALE,
                    AttributeModifier(fromString("crimson:golem_size")!!,
                        0.6, AttributeModifier.Operation.ADD_SCALAR)),
                PotionAbility(PotionEffect(PotionEffectType.STRENGTH, 1, 1, true, false)),
                PotionAbility(PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 1, 0, true, false)),
                PotionAbility(PotionEffect(PotionEffectType.SLOWNESS, 1, 1, true, false)),
                AttributeAbility(Attribute.MAX_HEALTH,
                    AttributeModifier(fromString("crimson:golem_health")!!,
                        1.0, AttributeModifier.Operation.ADD_SCALAR))
            ),
            3 to listOf(
                AttributeAbility(Attribute.SCALE,
                    AttributeModifier(fromString("crimson:golem_size")!!,
                        0.6, AttributeModifier.Operation.ADD_SCALAR)),
                PotionAbility(PotionEffect(PotionEffectType.STRENGTH, 1, 1, true, false)),
                PotionAbility(PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 1, 0, true, false)),
                PotionAbility(PotionEffect(PotionEffectType.SLOWNESS, 1, 1, true, false)),
                AttributeAbility(Attribute.MAX_HEALTH,
                    AttributeModifier(fromString("crimson:golem_health")!!,
                        1.5, AttributeModifier.Operation.ADD_SCALAR))
            )
        ),
        { Location(Bukkit.getWorld("world"), 0.0, 100.0, 0.0) }
    ),
    DWARF(
        translatable("menu.crimson.race_select.dwarf", "Dwarf", style(GOLD, ITALIC.withState(false))),
        mapOf(
            1 to listOf(
                text("1.35 blocks tall", style(GREEN, ITALIC.withState(false))),
                text("Faster mining", style(GREEN, ITALIC.withState(false))),
                text("-1 ❤", style(RED, ITALIC.withState(false)))
            ),
            2 to listOf(
                text("1.35 blocks tall", style(GREEN, ITALIC.withState(false))),
                text("Faster mining", style(GREEN, ITALIC.withState(false))),
                text("-1 ❤", style(RED, ITALIC.withState(false)))
            ),
            3 to listOf(
                text("1.35 blocks tall", style(GREEN, ITALIC.withState(false))),
                text("Faster mining", style(GREEN, ITALIC.withState(false))),
                text("-1 ❤", style(RED, ITALIC.withState(false)))
            )
        ),
        mapOf(
            1 to listOf(
                AttributeAbility(Attribute.SCALE,
                    AttributeModifier(fromString("crimson:dwarf_smallness")!!,
                        -0.25, AttributeModifier.Operation.ADD_SCALAR)),
                AttributeAbility(Attribute.MINING_EFFICIENCY,
                    AttributeModifier(fromString("crimson:fast_digger")!!,
                        1.0, AttributeModifier.Operation.ADD_NUMBER)),
                AttributeAbility(Attribute.MAX_HEALTH,
                    AttributeModifier(fromString("crimson:dwarf_health")!!,
                        -2.0, AttributeModifier.Operation.ADD_NUMBER))
            ),
            2 to listOf(
                AttributeAbility(Attribute.SCALE,
                    AttributeModifier(fromString("crimson:dwarf_smallness")!!,
                        -0.25, AttributeModifier.Operation.ADD_SCALAR)),
                AttributeAbility(Attribute.MINING_EFFICIENCY,
                    AttributeModifier(fromString("crimson:fast_digger")!!,
                        1.0, AttributeModifier.Operation.ADD_NUMBER)),
                AttributeAbility(Attribute.MAX_HEALTH,
                    AttributeModifier(fromString("crimson:dwarf_health")!!,
                        -2.0, AttributeModifier.Operation.ADD_NUMBER))
            ),
            3 to listOf(
                AttributeAbility(Attribute.SCALE,
                    AttributeModifier(fromString("crimson:dwarf_smallness")!!,
                        -0.25, AttributeModifier.Operation.ADD_SCALAR)),
                AttributeAbility(Attribute.MINING_EFFICIENCY,
                    AttributeModifier(fromString("crimson:fast_digger")!!,
                        1.0, AttributeModifier.Operation.ADD_NUMBER)),
                AttributeAbility(Attribute.MAX_HEALTH,
                    AttributeModifier(fromString("crimson:dwarf_health")!!,
                        -2.0, AttributeModifier.Operation.ADD_NUMBER))
            )
        ),
        { Location(Bukkit.getWorld("world"), 0.0, 100.0, 0.0) }
    ),
    DROWNED(
        translatable("menu.crimson.race_select.drowned", "Drowned", style(AQUA, ITALIC.withState(false))),
        mapOf(
            1 to listOf(
                text("Water Breathing", style(GREEN, ITALIC.withState(false))),
                text("Faster hunger drain when moving on land", style(RED, ITALIC.withState(false)))
            ),
            2 to listOf(
                text("Water Breathing", style(GREEN, ITALIC.withState(false))),
                text("Faster underwater mining", style(GREEN, ITALIC.withState(false))),
                text("Faster hunger drain when moving on land", style(RED, ITALIC.withState(false)))
            ),
            3 to listOf(
                text("Water Breathing", style(GREEN, ITALIC.withState(false))),
                text("Faster underwater mining", style(GREEN, ITALIC.withState(false))),
                text("Faster hunger drain when moving on land", style(RED, ITALIC.withState(false)))
            )
        ),
        mapOf(
            1 to listOf(
                PotionAbility(PotionEffect(PotionEffectType.WATER_BREATHING, 1, 0, true, false))
            ),
            2 to listOf(
                PotionAbility(PotionEffect(PotionEffectType.WATER_BREATHING, 1, 0, true, false)),
                AttributeAbility(Attribute.SUBMERGED_MINING_SPEED,
                    AttributeModifier(fromString("crimson:drowned_water_mining_speed")!!,
                        1.0, AttributeModifier.Operation.ADD_SCALAR))
            ),
            3 to listOf(
                PotionAbility(PotionEffect(PotionEffectType.WATER_BREATHING, 1, 0, true, false)),
                AttributeAbility(Attribute.SUBMERGED_MINING_SPEED,
                    AttributeModifier(fromString("crimson:drowned_water_mining_speed")!!,
                        2.0, AttributeModifier.Operation.ADD_SCALAR))
            )
        ),
        { Location(Bukkit.getWorld("world"), 0.0, 100.0, 0.0) }
    ),
    DEMON(
        translatable("menu.crimson.race_select.demon", "Demon", style(DARK_RED, ITALIC.withState(false))),
        mapOf(
            1 to listOf(
                text("Cannot stay on fire outside of fire and lava", style(GREEN, ITALIC.withState(false))),
                text("Swims slower", style(RED, ITALIC.withState(false))),
                text("Drowns instantly when out of air", style(RED, ITALIC.withState(false)))
            ),
            2 to listOf(
                text("+1 ❤", style(GREEN, ITALIC.withState(false))),
                text("Cannot stay on fire outside of fire and lava", style(GREEN, ITALIC.withState(false))),
                text("Swims slower", style(RED, ITALIC.withState(false))),
                text("Drowns instantly when out of air", style(RED, ITALIC.withState(false)))
            ),
            3 to listOf(
                text("+2 ❤", style(GREEN, ITALIC.withState(false))),
                text("Cannot stay on fire outside of fire and lava", style(GREEN, ITALIC.withState(false))),
                text("Swims slower", style(RED, ITALIC.withState(false))),
                text("Drowns instantly when out of air", style(RED, ITALIC.withState(false)))
            )
        ),
        mapOf(
            1 to listOf(
                AttributeAbility(Attribute.BURNING_TIME,
                    AttributeModifier(fromString("crimson:demon_burn_time")!!,
                        -1.0, AttributeModifier.Operation.ADD_SCALAR)),
                AttributeAbility(Attribute.WATER_MOVEMENT_EFFICIENCY,
                    AttributeModifier(fromString("crimson:demon_water_slowness")!!,
                        -1.0, AttributeModifier.Operation.ADD_SCALAR))
            ),
            2 to listOf(
                AttributeAbility(Attribute.BURNING_TIME,
                    AttributeModifier(fromString("crimson:demon_burn_time")!!,
                        -1.0, AttributeModifier.Operation.ADD_SCALAR)),
                AttributeAbility(Attribute.MAX_HEALTH,
                    AttributeModifier(fromString("crimson:demon_health")!!,
                        2.0, AttributeModifier.Operation.ADD_NUMBER)),
                AttributeAbility(Attribute.WATER_MOVEMENT_EFFICIENCY,
                    AttributeModifier(fromString("crimson:demon_water_slowness")!!,
                        -1.0, AttributeModifier.Operation.ADD_SCALAR))
            ),
            3 to listOf(
                AttributeAbility(Attribute.BURNING_TIME,
                    AttributeModifier(fromString("crimson:demon_burn_time")!!,
                        -1.0, AttributeModifier.Operation.ADD_SCALAR)),
                AttributeAbility(Attribute.MAX_HEALTH,
                    AttributeModifier(fromString("crimson:demon_health")!!,
                        4.0, AttributeModifier.Operation.ADD_NUMBER)),
                AttributeAbility(Attribute.WATER_MOVEMENT_EFFICIENCY,
                    AttributeModifier(fromString("crimson:demon_water_slowness")!!,
                        -1.0, AttributeModifier.Operation.ADD_SCALAR))
            )
        ),
        { Location(Bukkit.getWorld("world"), 0.0, 100.0, 0.0) }
    ),
    ENDER(
        translatable("menu.crimson.race_select.ender", "Ender", style(DARK_PURPLE, ITALIC.withState(false))),
        mapOf(
            1 to listOf(
                text("Drowns instantly when out of air", style(RED, ITALIC.withState(false))),
                text("2.5 blocks tall", style(RED, ITALIC.withState(false)))
            ),
            2 to listOf(
                text("+1 ❤", style(GREEN, ITALIC.withState(false))),
                text("Chorus fruit gives more hunger and saturation", style(GREEN, ITALIC.withState(false))),
                text("Drowns instantly when out of air", style(RED, ITALIC.withState(false))),
                text("2.5 blocks tall", style(RED, ITALIC.withState(false)))
            ),
            3 to listOf(
                text("+2 ❤", style(GREEN, ITALIC.withState(false))),
                text("Chorus fruit gives more hunger and saturation", style(GREEN, ITALIC.withState(false))),
                text("Drowns instantly when out of air", style(RED, ITALIC.withState(false))),
                text("2.5 blocks tall", style(RED, ITALIC.withState(false)))
            )
        ),
        mapOf(
            1 to listOf(
                AttributeAbility(Attribute.SCALE,
                    AttributeModifier(fromString("crimson:ender_height")!!,
                        0.3, AttributeModifier.Operation.ADD_SCALAR))
            ),
            2 to listOf(
                AttributeAbility(Attribute.SCALE,
                    AttributeModifier(fromString("crimson:ender_height")!!,
                        0.3, AttributeModifier.Operation.ADD_SCALAR)),
                AttributeAbility(Attribute.MAX_HEALTH,
                    AttributeModifier(fromString("crimson:ender_health")!!,
                        2.0, AttributeModifier.Operation.ADD_NUMBER))
            ),
            3 to listOf(
                AttributeAbility(Attribute.SCALE,
                    AttributeModifier(fromString("crimson:ender_height")!!,
                        0.3, AttributeModifier.Operation.ADD_SCALAR)),
                AttributeAbility(Attribute.MAX_HEALTH,
                    AttributeModifier(fromString("crimson:ender_health")!!,
                        4.0, AttributeModifier.Operation.ADD_NUMBER))
            )
        ),
        { Location(Bukkit.getWorld("world"), 0.0, 100.0, 0.0) }
    );

    init {
        require(abilityLores.keys.containsAll(listOf(1, 2, 3))) { "Invalid ability lore" }
        require(abilities.keys.containsAll(listOf(1, 2, 3))) { "Invalid abilities" }
    }
}
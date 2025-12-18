package me.kub94ek.crimson.races

import me.kub94ek.crimson.races.abilities.AttributeAbility
import me.kub94ek.crimson.races.abilities.PotionAbility
import me.kub94ek.crimson.races.abilities.RaceAbility
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

enum class Race(
    val displayName: Component,
    val abilityLores: Map<Int, List<List<Component>>>,
    val abilities: Map<Int, List<RaceAbility>>
) {
    HUMAN(
        Component.translatable("menu.crimson.race_select.human", "Human"),
        mapOf(
            1 to listOf(listOf(Component.translatable("gui.none"))),
            2 to listOf(listOf(Component.translatable("gui.none"))),
            3 to listOf(listOf(Component.translatable("gui.none")))
        ),
        mapOf(1 to listOf(), 2 to listOf(), 3 to listOf())
    ),
    TEST(
        Component.translatable("menu.crimson.race_select.test", "Test"),
        mapOf(
            1 to listOf(listOf(Component.translatable("gui.none"))),
            2 to listOf(listOf(Component.translatable("gui.none"))),
            3 to listOf(createAbilityLore(
                Component.text("Test ability"), Component.text("Does absolutely nothing - like myself"),
                20, 30
            ))
        ),
        mapOf(
            1 to listOf(
                PotionAbility(PotionEffect(PotionEffectType.STRENGTH, 1, 5)),
                AttributeAbility(Attribute.SCALE, AttributeModifier(NamespacedKey.fromString("crimson:test")!!, 1.0, AttributeModifier.Operation.ADD_SCALAR))
            ),
            2 to listOf(),
            3 to listOf()
        )
    );

    init {
        require(abilityLores.keys.containsAll(listOf(1, 2, 3))) { "Invalid ability lore" }
        require(abilities.keys.containsAll(listOf(1, 2, 3))) { "Invalid abilities" }
    }
}
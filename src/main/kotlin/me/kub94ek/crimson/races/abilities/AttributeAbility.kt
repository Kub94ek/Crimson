package me.kub94ek.crimson.races.abilities

import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier

data class AttributeAbility(
    val attribute: Attribute, val modifier: AttributeModifier
): RaceAbility
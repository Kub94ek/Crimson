@file:Suppress("UnstableApiUsage")

package me.kub94ek.crimson

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import me.kub94ek.crimson.command.crimson.*
import me.kub94ek.crimson.command.race.*
import me.kub94ek.crimson.listeners.*
import me.kub94ek.crimson.races.Race
import me.kub94ek.crimson.races.getRaceLevel
import me.kub94ek.crimson.races.getRaceType
import me.kub94ek.crimson.races.setRaceType
import me.kub94ek.kubLib.KubLib
import me.kub94ek.kubLib.cooldown.CooldownCommand
import me.kub94ek.kubLib.gui.ChestMenu
import me.kub94ek.kubLib.gui.MenuItem
import me.kub94ek.kubLib.items.CustomDrops
import me.kub94ek.kubLib.items.ItemRegistry
import me.kub94ek.kubLib.items.effects.ArmorSetEffect
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey.fromString
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.util.UUID

class Crimson : JavaPlugin() {

    private val selectedRaces = mutableMapOf<UUID, String>()
    private val RACE_MATERIALS = mapOf(
        "human" to Material.PLAYER_HEAD,
        "golem" to Material.IRON_BLOCK,
        "dwarf" to Material.IRON_PICKAXE,
        "drowned" to Material.NAUTILUS_SHELL,
        "demon" to Material.MAGMA_BLOCK,
        "ender" to Material.ENDER_PEARL
    )

    var economy: Economy? = null
    val recipesToUnlock = mutableListOf<String>()

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        logger.info("Starting...")
        componentLogger.info(text("+-------------------+", NamedTextColor.GREEN))
        componentLogger.info(
            text("|      ", NamedTextColor.GREEN)
                .append(text("Crimson", TextColor.color(0xDC143C)))
                .append(text("      |", NamedTextColor.GREEN))
        )
        componentLogger.info(
            text("|     ", NamedTextColor.GREEN)
                .append(text("by Kub94ek", NamedTextColor.AQUA))
                .append(text("    |", NamedTextColor.GREEN))
        )
        componentLogger.info(text("+-------------------+", NamedTextColor.GREEN))

        if (!setupEconomy()) {
            logger.severe("[Crimson] - Disabled due to no Vault dependency found!")
            server.pluginManager.disablePlugin(this)
            return
        }

        config.options().copyDefaults()
        saveDefaultConfig()

        val kubLib = KubLib.getInstance()

        registerListeners(kubLib)
        registerItems(kubLib)
        registerRecipes(kubLib)
        registerCustomDrops(kubLib.itemRegistry)
        registerMenus(kubLib)
        registerCommands()

        kubLib.cooldownManager.registerCooldownType("crimson:ender_teleport",
            text("You can use your Ender Armor Teleport ability again."), 30)

    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) return false
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        economy = rsp.provider
        return true
    }

    override fun onDisable() {
        logger.info("Shutting down...")
    }

    private fun registerListeners(kubLib: KubLib) {
        val pluginManager = server.pluginManager
        pluginManager.registerEvents(BanHammerListener(kubLib.itemRegistry), this)
        pluginManager.registerEvents(RaceListener(kubLib), this)
        pluginManager.registerEvents(EnderArmorListener(), this)
        pluginManager.registerEvents(NetherEventListener(), this)
        pluginManager.registerEvents(WitherArmorListener(), this)
    }

    private fun registerItems(kubLib: KubLib) {
        val itemRegistry = kubLib.itemRegistry


        val echoCrystal = ItemRegistry.createItem(
            ECHO_CRYSTAL, Material.QUARTZ,
            translatable(translationString(ECHO_CRYSTAL), "Echo Crystal", NamedTextColor.DARK_AQUA)
        )
        echoCrystal.editMeta {
            it.setEnchantmentGlintOverride(true)
        }
        itemRegistry.registerItem(ECHO_CRYSTAL, echoCrystal)

        val echoIngot = ItemRegistry.createItem(
            ECHO_INGOT, Material.NETHERITE_INGOT,
            translatable(translationString(ECHO_INGOT), "Echo Ingot", NamedTextColor.DARK_AQUA)
        )
        echoIngot.editMeta {
            it.setEnchantmentGlintOverride(true)
        }
        itemRegistry.registerItem(ECHO_INGOT, echoIngot)

        val echoUpgrade = ItemRegistry.createItem(
            ECHO_UPGRADE, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
            translatable(translationString(ECHO_UPGRADE), "Echo Upgrade", NamedTextColor.DARK_AQUA)
        )
        echoUpgrade.editMeta {
            it.setEnchantmentGlintOverride(true)
        }
        itemRegistry.registerItem(ECHO_UPGRADE, echoUpgrade)

        itemRegistry.registerItem(
            PRISMARINE_ALLOY, Material.PRISMARINE_SHARD,
            translatable(translationString(PRISMARINE_ALLOY), "Prismarine Alloy", PRISMARINE)
        )

        val crackedWitherHelmet = ItemRegistry.createItem(
            CRACKED_WITHER_HELMET, Material.IRON_NUGGET,
            translatable(translationString(CRACKED_WITHER_HELMET), "Cracked Wither Helmet", NamedTextColor.DARK_RED),
            listOf(
                text("Used to craft wither armor", style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false)))
            )
        )
        itemRegistry.registerItem(CRACKED_WITHER_HELMET, crackedWitherHelmet)

        val crackedWitherChestplate = ItemRegistry.createItem(
            CRACKED_WITHER_CHESTPLATE, Material.IRON_NUGGET,
            translatable(translationString(CRACKED_WITHER_CHESTPLATE), "Cracked Wither Chestplate", NamedTextColor.DARK_RED),
            listOf(
                text("Used to craft wither armor", style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false)))
            )
        )
        itemRegistry.registerItem(CRACKED_WITHER_CHESTPLATE, crackedWitherChestplate)

        val crackedWitherLeggings = ItemRegistry.createItem(
            CRACKED_WITHER_LEGGINGS, Material.IRON_NUGGET,
            translatable(translationString(CRACKED_WITHER_LEGGINGS), "Cracked Wither Leggings", NamedTextColor.DARK_RED),
            listOf(
                text("Used to craft wither armor", style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false)))
            )
        )
        itemRegistry.registerItem(CRACKED_WITHER_LEGGINGS, crackedWitherLeggings)

        val crackedWitherBoots = ItemRegistry.createItem(
            CRACKED_WITHER_BOOTS, Material.IRON_NUGGET,
            translatable(translationString(CRACKED_WITHER_BOOTS), "Cracked Wither Boots", NamedTextColor.DARK_RED),
            listOf(
                text("Used to craft wither armor", style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false)))
            )
        )
        itemRegistry.registerItem(CRACKED_WITHER_BOOTS, crackedWitherBoots)

        registerArmor(kubLib)
        registerRaceItems(kubLib)
    }

    private fun registerArmor(kubLib: KubLib) {
        val itemRegistry = kubLib.itemRegistry

        /*val echoHelmet = ItemRegistry.createArmor(
            ECHO_HELMET, "crimson:echo", Material.NETHERITE_HELMET,
            translatable(translationString(ECHO_HELMET), "Echo Helmet", NamedTextColor.DARK_AQUA),
            EquipmentSlot.HEAD
        )
        itemRegistry.registerItem(ECHO_HELMET, echoHelmet)

        val echoChestplate = ItemRegistry.createArmor(
            ECHO_CHESTPLATE, "crimson:echo", Material.NETHERITE_CHESTPLATE,
            translatable(translationString(ECHO_CHESTPLATE), "Echo Chestplate", NamedTextColor.DARK_AQUA),
            EquipmentSlot.CHEST
        )
        itemRegistry.registerItem(ECHO_CHESTPLATE, echoChestplate)

        val echoLeggings = ItemRegistry.createArmor(
            ECHO_LEGGINGS, "crimson:echo", Material.NETHERITE_LEGGINGS,
            translatable(translationString(ECHO_LEGGINGS), "Echo Leggings", NamedTextColor.DARK_AQUA),
            EquipmentSlot.LEGS
        )
        itemRegistry.registerItem(ECHO_LEGGINGS, echoLeggings)

        val echoBoots = ItemRegistry.createArmor(
            ECHO_BOOTS, "crimson:echo", Material.NETHERITE_BOOTS,
            translatable(translationString(ECHO_BOOTS), "Echo Boots", NamedTextColor.DARK_AQUA),
            EquipmentSlot.FEET
        )
        itemRegistry.registerItem(ECHO_BOOTS, echoBoots)


        val atlantisHelmet = ItemRegistry.createArmor(
            ATLANTIS_HELMET, "crimson:atlantis", Material.DIAMOND_HELMET,
            translatable(translationString(ATLANTIS_HELMET), "Atlantis Helmet", PRISMARINE),
            EquipmentSlot.HEAD
        )
        itemRegistry.registerItem(ATLANTIS_HELMET, atlantisHelmet)

        val atlantisChestplate = ItemRegistry.createArmor(
            ATLANTIS_CHESTPLATE, "crimson:atlantis", Material.DIAMOND_CHESTPLATE,
            translatable(translationString(ATLANTIS_CHESTPLATE), "Atlantis Chestplate", PRISMARINE),
            EquipmentSlot.CHEST
        )
        itemRegistry.registerItem(ATLANTIS_CHESTPLATE, atlantisChestplate)

        val atlantisLeggings = ItemRegistry.createArmor(
            ATLANTIS_LEGGINGS, "crimson:atlantis", Material.DIAMOND_LEGGINGS,
            translatable(translationString(ATLANTIS_LEGGINGS), "Atlantis Leggings", PRISMARINE),
            EquipmentSlot.LEGS
        )
        itemRegistry.registerItem(ATLANTIS_LEGGINGS, atlantisLeggings)

        val atlantisBoots = ItemRegistry.createArmor(
            ATLANTIS_BOOTS, "crimson:atlantis", Material.DIAMOND_BOOTS,
            translatable(translationString(ATLANTIS_BOOTS), "Atlantis Boots", PRISMARINE),
            EquipmentSlot.FEET
        )
        itemRegistry.registerItem(ATLANTIS_BOOTS, atlantisBoots)*/


        val enderHelmet = ItemRegistry.createArmor(
            ENDER_HELMET, "crimson:ender", Material.DIAMOND_HELMET,
            translatable(translationString(ENDER_HELMET), "Ender Helmet", ENDER),
            EquipmentSlot.HEAD
        )
        enderHelmet.editMeta {
            it.lore(listOf(
                text("Set Bonus: Shift right-click with a sword", style(ENDER, TextDecoration.ITALIC.withState(false))),
                text("to teleport up to 6 blocks ahead", style(ENDER, TextDecoration.ITALIC.withState(false))),
                text("Cooldown: 30s", style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(ENDER_HELMET, enderHelmet)
        itemRegistry.registerItem("${ENDER_HELMET}_netherite", enderHelmet.withType(Material.NETHERITE_HELMET))

        val enderChestplate = ItemRegistry.createArmor(
            ENDER_CHESTPLATE, "crimson:ender", Material.DIAMOND_CHESTPLATE,
            translatable(translationString(ENDER_CHESTPLATE), "Ender Chestplate", ENDER),
            EquipmentSlot.CHEST
        )
        enderChestplate.editMeta {
            it.lore(listOf(
                text("Can be used as an elytra", style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false))),
                text(""),
                text("Set Bonus: Shift right-click with a sword", style(ENDER, TextDecoration.ITALIC.withState(false))),
                text("to teleport up to 6 blocks ahead", style(ENDER, TextDecoration.ITALIC.withState(false))),
                text("Cooldown: 30s", style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false)))
            ))
            it.isGlider = true
        }
        itemRegistry.registerItem(ENDER_CHESTPLATE, enderChestplate)
        itemRegistry.registerItem("${ENDER_CHESTPLATE}_netherite", enderChestplate.withType(Material.NETHERITE_CHESTPLATE))

        val enderLeggings = ItemRegistry.createArmor(
            ENDER_LEGGINGS, "crimson:ender", Material.DIAMOND_LEGGINGS,
            translatable(translationString(ENDER_LEGGINGS), "Ender Leggings", ENDER),
            EquipmentSlot.LEGS
        )
        enderLeggings.editMeta {
            it.lore(listOf(
                text("Set Bonus: Shift right-click with a sword", style(ENDER, TextDecoration.ITALIC.withState(false))),
                text("to teleport up to 6 blocks ahead", style(ENDER, TextDecoration.ITALIC.withState(false))),
                text("Cooldown: 30s", style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(ENDER_LEGGINGS, enderLeggings)
        itemRegistry.registerItem("${ENDER_LEGGINGS}_netherite", enderLeggings.withType(Material.NETHERITE_LEGGINGS))

        val enderBoots = ItemRegistry.createArmor(
            ENDER_BOOTS, "crimson:ender", Material.DIAMOND_BOOTS,
            translatable(translationString(ENDER_BOOTS), "Ender Boots", ENDER),
            EquipmentSlot.FEET
        )
        enderBoots.editMeta {
            it.lore(listOf(
                text("Set Bonus: Shift right-click with a sword", style(ENDER, TextDecoration.ITALIC.withState(false))),
                text("to teleport up to 6 blocks ahead", style(ENDER, TextDecoration.ITALIC.withState(false))),
                text("Cooldown: 30s", style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(ENDER_BOOTS, enderBoots)
        itemRegistry.registerItem("${ENDER_BOOTS}_netherite", enderBoots.withType(Material.NETHERITE_BOOTS))


        val dwarfHelmet = ItemRegistry.createArmor(
            DWARF_HELMET, "crimson:dwarf", Material.DIAMOND_HELMET,
            translatable(translationString(DWARF_HELMET), "Dwarf Helmet", NamedTextColor.GOLD),
            EquipmentSlot.HEAD
        )
        dwarfHelmet.editMeta {
            it.lore(listOf(
                text("Set bonus: Haste II", style(NamedTextColor.YELLOW, TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(DWARF_HELMET, dwarfHelmet)

        val dwarfChestplate = ItemRegistry.createArmor(
            DWARF_CHESTPLATE, "crimson:dwarf", Material.DIAMOND_CHESTPLATE,
            translatable(translationString(DWARF_CHESTPLATE), "Dwarf Chestplate", NamedTextColor.GOLD),
            EquipmentSlot.CHEST
        )
        dwarfChestplate.editMeta {
            it.lore(listOf(
                text("Set bonus: Haste II", style(NamedTextColor.YELLOW, TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(DWARF_CHESTPLATE, dwarfChestplate)

        val dwarfLeggings = ItemRegistry.createArmor(
            DWARF_LEGGINGS, "crimson:dwarf", Material.DIAMOND_LEGGINGS,
            translatable(translationString(DWARF_LEGGINGS), "Dwarf Leggings", NamedTextColor.GOLD),
            EquipmentSlot.LEGS
        )
        dwarfLeggings.editMeta {
            it.lore(listOf(
                text("Set bonus: Haste II", style(NamedTextColor.YELLOW, TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(DWARF_LEGGINGS, dwarfLeggings)

        val dwarfBoots = ItemRegistry.createArmor(
            DWARF_BOOTS, "crimson:dwarf", Material.DIAMOND_BOOTS,
            translatable(translationString(DWARF_BOOTS), "Dwarf Boots", NamedTextColor.GOLD),
            EquipmentSlot.FEET
        )
        dwarfBoots.editMeta {
            it.lore(listOf(
                text("Set bonus: Haste II", style(NamedTextColor.YELLOW, TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(DWARF_BOOTS, dwarfBoots)

        itemRegistry.registerItemEffect(
            object : ArmorSetEffect(DWARF_BOOTS, DWARF_LEGGINGS, DWARF_CHESTPLATE, DWARF_HELMET, 20) {
                override fun applyEffect(player: Player) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 40, 1, true, false, true))
                }
            }
        )


        val witherHelmet = ItemRegistry.createArmor(
            WITHER_HELMET, "crimson:wither", Material.NETHERITE_HELMET,
            translatable(translationString(WITHER_HELMET), "Wither Helmet", NamedTextColor.DARK_RED),
            EquipmentSlot.HEAD
        )
        witherHelmet.editMeta {
            it.lore(listOf(
                text("Set bonus: 60% chance to inflict", style(TextColor.color(0xcf0000), TextDecoration.ITALIC.withState(false))),
                text("Wither on the attacker", style(TextColor.color(0xcf0000), TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(WITHER_HELMET, witherHelmet)

        val witherChestplate = ItemRegistry.createArmor(
            WITHER_CHESTPLATE, "crimson:wither", Material.NETHERITE_CHESTPLATE,
            translatable(translationString(WITHER_CHESTPLATE), "Wither Chestplate", NamedTextColor.DARK_RED),
            EquipmentSlot.CHEST
        )
        witherChestplate.editMeta {
            it.lore(listOf(
                text("Set bonus: 60% chance to inflict", style(TextColor.color(0xcf0000), TextDecoration.ITALIC.withState(false))),
                text("Wither on the attacker", style(TextColor.color(0xcf0000), TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(WITHER_CHESTPLATE, witherChestplate)

        val witherLeggings = ItemRegistry.createArmor(
            WITHER_LEGGINGS, "crimson:wither", Material.NETHERITE_LEGGINGS,
            translatable(translationString(WITHER_LEGGINGS), "Wither Leggings", NamedTextColor.DARK_RED),
            EquipmentSlot.LEGS
        )
        witherLeggings.editMeta {
            it.lore(listOf(
                text("Set bonus: 60% chance to inflict", style(TextColor.color(0xcf0000), TextDecoration.ITALIC.withState(false))),
                text("Wither on the attacker", style(TextColor.color(0xcf0000), TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(WITHER_LEGGINGS, witherLeggings)

        val witherBoots = ItemRegistry.createArmor(
            WITHER_BOOTS, "crimson:wither", Material.NETHERITE_BOOTS,
            translatable(translationString(WITHER_BOOTS), "Wither Boots", NamedTextColor.DARK_RED),
            EquipmentSlot.FEET
        )
        witherBoots.editMeta {
            it.lore(listOf(
                text("Set bonus: 60% chance to inflict", style(TextColor.color(0xcf0000), TextDecoration.ITALIC.withState(false))),
                text("Wither on the attacker", style(TextColor.color(0xcf0000), TextDecoration.ITALIC.withState(false)))
            ))
        }
        itemRegistry.registerItem(WITHER_BOOTS, witherBoots)



        /*itemRegistry.registerItemEffect(object : ArmorSetEffect(ATLANTIS_BOOTS, ATLANTIS_LEGGINGS, ATLANTIS_CHESTPLATE, ATLANTIS_HELMET, 20) {
            override fun applyEffect(player: Player) {
                player.addPotionEffect(PotionEffect(PotionEffectType.CONDUIT_POWER, 40, 0, true, false, true))
            }
        })

        itemRegistry.registerItemEffect(object : SlotEffect(ATLANTIS_BOOTS, 20, EquipmentSlot.FEET) {
            override fun applyEffect(player: Player) {
                player.addPotionEffect(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, true, false, true))
            }
        })*/

    }

    private fun registerRaceItems(kubLib: KubLib) {
        val itemRegistry = kubLib.itemRegistry

        itemRegistry.registerItem("crimson:race_selector_human", Material.PLAYER_HEAD,
            Race.HUMAN.displayName,
            listOf(
                text("Spawns in a forest", style(NamedTextColor.YELLOW, TextDecoration.ITALIC.withState(false))),
                text(""),
                text("No buffs, no debuffs", style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false)))
            ), null
        )
        itemRegistry.registerItem("crimson:race_selector_golem", Material.IRON_BLOCK,
            Race.GOLEM.displayName,
            listOf(
                text("Spawns in a village", style(NamedTextColor.YELLOW, TextDecoration.ITALIC.withState(false))),
                text(""),
                text("+5/10/15 ❤", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("Hero of the Village I", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("Strength I/II/II", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("Slowness I/II/II", style(NamedTextColor.RED, TextDecoration.ITALIC.withState(false))),
                text("3 blocks tall", style(NamedTextColor.RED, TextDecoration.ITALIC.withState(false)))
            ), null
        )
        itemRegistry.registerItem("crimson:race_selector_dwarf", Material.IRON_PICKAXE,
            Race.DWARF.displayName,
            listOf(
                text("Spawns in a mountain cave", style(NamedTextColor.YELLOW, TextDecoration.ITALIC.withState(false))),
                text(""),
                text("1.35 blocks tall", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("Faster mining (scales with levels)", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("-1 ❤", style(NamedTextColor.RED, TextDecoration.ITALIC.withState(false)))
            ), null
        )
        itemRegistry.registerItem("crimson:race_selector_drowned", Material.NAUTILUS_SHELL,
            Race.DROWNED.displayName,
            listOf(
                text("Spawns on the beach", style(NamedTextColor.YELLOW, TextDecoration.ITALIC.withState(false))),
                text(""),
                text("Water Breathing", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("Faster underwater mining", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("Faster hunger drain when moving on land", style(NamedTextColor.RED, TextDecoration.ITALIC.withState(false)))
            ), null
        )
        itemRegistry.registerItem("crimson:race_selector_demon", Material.MAGMA_BLOCK,
            Race.DEMON.displayName,
            listOf(
                text("Spawns next to a ruined portal", style(NamedTextColor.YELLOW, TextDecoration.ITALIC.withState(false))),
                text(""),
                text("+0/1/2 ❤", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("Cannot stay on fire outside of fire and lava", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("Swims slower", style(NamedTextColor.RED, TextDecoration.ITALIC.withState(false))),
                text("Drowns instantly when out of air", style(NamedTextColor.RED, TextDecoration.ITALIC.withState(false)))
            ), null
        )
        itemRegistry.registerItem("crimson:race_selector_ender", Material.ENDER_PEARL,
            Race.ENDER.displayName,
            listOf(
                text("Spawns in a pale garden", style(NamedTextColor.YELLOW, TextDecoration.ITALIC.withState(false))),
                text(""),
                text("+0/1/2 ❤", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("Chorus fruit gives more hunger and saturation", style(NamedTextColor.GREEN, TextDecoration.ITALIC.withState(false))),
                text("Drowns instantly when out of air", style(NamedTextColor.RED, TextDecoration.ITALIC.withState(false))),
                text("2.5 blocks tall", style(NamedTextColor.RED, TextDecoration.ITALIC.withState(false)))

            ), null
        )

    }

    private fun registerRecipes(kubLib: KubLib) {
        val itemRegistry = kubLib.itemRegistry
        val recipeRegistry = kubLib.recipeRegistry

        val echoIngotRecipe = ShapedRecipe(
            fromString(ECHO_INGOT)!!,
            itemRegistry.getItemStack(ECHO_INGOT)!!
        )
        echoIngotRecipe.shape(
            "SCS",
            "CIC",
            "SCS"
        )
        echoIngotRecipe.setIngredient('S', Material.ECHO_SHARD)
        echoIngotRecipe.setIngredient('C', itemRegistry.getItemStack(ECHO_CRYSTAL)!!)
        echoIngotRecipe.setIngredient('I', Material.NETHERITE_INGOT)
        recipeRegistry.registerRecipe(echoIngotRecipe)
        recipesToUnlock.add(ECHO_INGOT)

        val echoUpgradeDupeRecipe = ShapedRecipe(
            fromString(ECHO_UPGRADE)!!,
            itemRegistry.getItemStack(ECHO_UPGRADE)!!.asQuantity(2)
        )
        echoUpgradeDupeRecipe.shape(
            "CUC",
            "CSC",
            "CCC"
        )
        echoUpgradeDupeRecipe.setIngredient('C', itemRegistry.getItemStack(ECHO_CRYSTAL)!!)
        echoUpgradeDupeRecipe.setIngredient('U', itemRegistry.getItemStack(ECHO_UPGRADE)!!)
        echoUpgradeDupeRecipe.setIngredient('S', Material.SCULK)
        recipeRegistry.registerRecipe(echoUpgradeDupeRecipe)
        recipesToUnlock.add(ECHO_UPGRADE)


        /*recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(ECHO_HELMET)!!,
            itemRegistry.getItemStack(ECHO_HELMET)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_UPGRADE)!!),
            RecipeChoice.itemType(ItemType.NETHERITE_HELMET),
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_INGOT)!!)
        ))

        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(ECHO_CHESTPLATE)!!,
            itemRegistry.getItemStack(ECHO_CHESTPLATE)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_UPGRADE)!!),
            RecipeChoice.itemType(ItemType.NETHERITE_CHESTPLATE),
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_INGOT)!!)
        ))

        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(ECHO_LEGGINGS)!!,
            itemRegistry.getItemStack(ECHO_LEGGINGS)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_UPGRADE)!!),
            RecipeChoice.itemType(ItemType.NETHERITE_LEGGINGS),
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_INGOT)!!)
        ))

        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(ECHO_BOOTS)!!,
            itemRegistry.getItemStack(ECHO_BOOTS)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_UPGRADE)!!),
            RecipeChoice.itemType(ItemType.NETHERITE_BOOTS),
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_INGOT)!!)
        ))

        val prismarineAlloyRecipe = ShapedRecipe(
            fromString(PRISMARINE_ALLOY)!!,
            itemRegistry.getItemStack(PRISMARINE_ALLOY)!!
        )
        prismarineAlloyRecipe.shape(
            "SCS",
            "CIC",
            "SCS"
        )
        prismarineAlloyRecipe.setIngredient('S', Material.PRISMARINE_SHARD)
        prismarineAlloyRecipe.setIngredient('C', Material.PRISMARINE_CRYSTALS)
        prismarineAlloyRecipe.setIngredient('I', Material.IRON_INGOT)
        recipeRegistry.registerRecipe(prismarineAlloyRecipe)


        val atlantisChestplateRecipe = ShapedRecipe(
            fromString(ATLANTIS_CHESTPLATE)!!,
            itemRegistry.getItemStack(ATLANTIS_CHESTPLATE)!!
        )
        atlantisChestplateRecipe.shape(
            "A A",
            "AHA",
            "AAA"
        )
        atlantisChestplateRecipe.setIngredient('H', Material.HEART_OF_THE_SEA)
        atlantisChestplateRecipe.setIngredient('A', itemRegistry.getItemStack(PRISMARINE_ALLOY)!!)
        recipeRegistry.registerRecipe(atlantisChestplateRecipe)*/

        val dwarfHelmetRecipe = ShapedRecipe(
            fromString(DWARF_HELMET)!!,
            itemRegistry.getItemStack(DWARF_HELMET)!!
        )
        dwarfHelmetRecipe.shape(
            "GRG",
            "TAT",
            "GRG"
        )
        dwarfHelmetRecipe.setIngredient('G', Material.GOLD_INGOT)
        dwarfHelmetRecipe.setIngredient('R', Material.REDSTONE)
        dwarfHelmetRecipe.setIngredient('T', Material.TORCH)
        dwarfHelmetRecipe.setIngredient('A', Material.DIAMOND_HELMET)
        recipeRegistry.registerRecipe(dwarfHelmetRecipe)

        val dwarfChestplateRecipe = ShapedRecipe(
            fromString(DWARF_CHESTPLATE)!!,
            itemRegistry.getItemStack(DWARF_CHESTPLATE)!!
        )
        dwarfChestplateRecipe.shape(
            "GCG",
            "DAD",
            "GDG"
        )
        dwarfChestplateRecipe.setIngredient('G', Material.GOLD_INGOT)
        dwarfChestplateRecipe.setIngredient('C', Material.CLOCK)
        dwarfChestplateRecipe.setIngredient('D', Material.DIAMOND)
        dwarfChestplateRecipe.setIngredient('A', Material.DIAMOND_CHESTPLATE)
        recipeRegistry.registerRecipe(dwarfChestplateRecipe)

        val dwarfLeggingsRecipe = ShapedRecipe(
            fromString(DWARF_LEGGINGS)!!,
            itemRegistry.getItemStack(DWARF_LEGGINGS)!!
        )
        dwarfLeggingsRecipe.shape(
            "RDR",
            "IAG",
            "RDR"
        )
        dwarfLeggingsRecipe.setIngredient('R', Material.REDSTONE)
        dwarfLeggingsRecipe.setIngredient('D', Material.DIAMOND)
        dwarfLeggingsRecipe.setIngredient('I', Material.IRON_PICKAXE)
        dwarfLeggingsRecipe.setIngredient('A', Material.DIAMOND_LEGGINGS)
        dwarfLeggingsRecipe.setIngredient('G', Material.GOLDEN_PICKAXE)
        recipeRegistry.registerRecipe(dwarfLeggingsRecipe)

        val dwarfBootsRecipe = ShapedRecipe(
            fromString(DWARF_BOOTS)!!,
            itemRegistry.getItemStack(DWARF_BOOTS)!!
        )
        dwarfBootsRecipe.shape(
            "IGI",
            "CAC",
            "GIG"
        )
        dwarfBootsRecipe.setIngredient('I', Material.IRON_INGOT)
        dwarfBootsRecipe.setIngredient('G', Material.GOLD_INGOT)
        dwarfBootsRecipe.setIngredient('C', Material.IRON_CHAIN)
        dwarfBootsRecipe.setIngredient('A', Material.DIAMOND_BOOTS)
        recipeRegistry.registerRecipe(dwarfBootsRecipe)

        recipesToUnlock.add(DWARF_HELMET)
        recipesToUnlock.add(DWARF_CHESTPLATE)
        recipesToUnlock.add(DWARF_LEGGINGS)
        recipesToUnlock.add(DWARF_BOOTS)


        val enderHelmet = ShapedRecipe(
            fromString(ENDER_HELMET)!!,
            itemRegistry.getItemStack(ENDER_HELMET)!!
        )
        enderHelmet.shape(
            "SHS",
            "EAE",
            "SPS"
        )
        enderHelmet.setIngredient('S', Material.END_STONE)
        enderHelmet.setIngredient('H', Material.DRAGON_HEAD)
        enderHelmet.setIngredient('E', Material.ENDER_EYE)
        enderHelmet.setIngredient('A', Material.DIAMOND_HELMET)
        enderHelmet.setIngredient('P', Material.ENDER_PEARL)
        recipeRegistry.registerRecipe(enderHelmet)

        val enderChestplate = ShapedRecipe(
            fromString(ENDER_CHESTPLATE)!!,
            itemRegistry.getItemStack(ENDER_CHESTPLATE)!!
        )
        enderChestplate.shape(
            "DED",
            "YAY",
            "SSS"
        )
        enderChestplate.setIngredient('D', Material.DIAMOND)
        enderChestplate.setIngredient('E', Material.ELYTRA)
        enderChestplate.setIngredient('Y', Material.ENDER_EYE)
        enderChestplate.setIngredient('A', Material.DIAMOND_CHESTPLATE)
        enderChestplate.setIngredient('S', Material.END_STONE)
        recipeRegistry.registerRecipe(enderChestplate)

        val enderLeggings = ShapedRecipe(
            fromString(ENDER_LEGGINGS)!!,
            itemRegistry.getItemStack(ENDER_LEGGINGS)!!
        )
        enderLeggings.shape(
            "FEF",
            "CAC",
            "FEF"
        )
        enderLeggings.setIngredient('F', Material.CHORUS_FRUIT)
        enderLeggings.setIngredient('E', Material.END_STONE)
        enderLeggings.setIngredient('C', Material.END_CRYSTAL)
        enderLeggings.setIngredient('A', Material.DIAMOND_LEGGINGS)
        recipeRegistry.registerRecipe(enderLeggings)

        val enderBoots = ShapedRecipe(
            fromString(ENDER_BOOTS)!!,
            itemRegistry.getItemStack(ENDER_BOOTS)!!
        )
        enderBoots.shape(
            "PDP",
            "DAD",
            "PDP"
        )
        enderBoots.setIngredient('P', Material.ENDER_PEARL)
        enderBoots.setIngredient('D', Material.DIAMOND)
        enderBoots.setIngredient('A', Material.DIAMOND_BOOTS)
        recipeRegistry.registerRecipe(enderBoots)

        recipesToUnlock.add(ENDER_HELMET)
        recipesToUnlock.add(ENDER_CHESTPLATE)
        recipesToUnlock.add(ENDER_LEGGINGS)
        recipesToUnlock.add(ENDER_BOOTS)


        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(WITHER_HELMET)!!,
            itemRegistry.getItemStack(WITHER_HELMET)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(CRACKED_WITHER_HELMET)!!),
            RecipeChoice.itemType(ItemType.NETHERITE_HELMET),
            RecipeChoice.itemType(ItemType.NETHERITE_INGOT)
        ))

        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(WITHER_CHESTPLATE)!!,
            itemRegistry.getItemStack(WITHER_CHESTPLATE)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(CRACKED_WITHER_CHESTPLATE)!!),
            RecipeChoice.itemType(ItemType.NETHERITE_CHESTPLATE),
            RecipeChoice.itemType(ItemType.NETHERITE_INGOT)
        ))

        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(WITHER_LEGGINGS)!!,
            itemRegistry.getItemStack(WITHER_LEGGINGS)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(CRACKED_WITHER_LEGGINGS)!!),
            RecipeChoice.itemType(ItemType.NETHERITE_LEGGINGS),
            RecipeChoice.itemType(ItemType.NETHERITE_INGOT)
        ))

        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(WITHER_BOOTS)!!,
            itemRegistry.getItemStack(WITHER_BOOTS)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(CRACKED_WITHER_BOOTS)!!),
            RecipeChoice.itemType(ItemType.NETHERITE_BOOTS),
            RecipeChoice.itemType(ItemType.NETHERITE_INGOT)
        ))


        val crackedWitherHelmet = ShapedRecipe(
            fromString(CRACKED_WITHER_HELMET)!!,
            itemRegistry.getItemStack(CRACKED_WITHER_HELMET)!!
        )
        crackedWitherHelmet.shape(
            "RNR",
            "MSM",
            "RNR"
        )
        crackedWitherHelmet.setIngredient('R', Material.WITHER_ROSE)
        crackedWitherHelmet.setIngredient('N', Material.NETHERITE_SCRAP)
        crackedWitherHelmet.setIngredient('M', Material.MAGMA_BLOCK)
        crackedWitherHelmet.setIngredient('S', Material.WITHER_SKELETON_SKULL)
        recipeRegistry.registerRecipe(crackedWitherHelmet)

        val crackedWitherChestplate = ShapedRecipe(
            fromString(CRACKED_WITHER_CHESTPLATE)!!,
            itemRegistry.getItemStack(CRACKED_WITHER_CHESTPLATE)!!
        )
        crackedWitherChestplate.shape(
            "R R",
            "NSN",
            "MRM"
        )
        crackedWitherChestplate.setIngredient('R', Material.WITHER_ROSE)
        crackedWitherChestplate.setIngredient('N', Material.NETHERITE_SCRAP)
        crackedWitherChestplate.setIngredient('M', Material.MAGMA_BLOCK)
        crackedWitherChestplate.setIngredient('S', Material.NETHER_STAR)
        recipeRegistry.registerRecipe(crackedWitherChestplate)

        val crackedWitherLeggings = ShapedRecipe(
            fromString(CRACKED_WITHER_LEGGINGS)!!,
            itemRegistry.getItemStack(CRACKED_WITHER_LEGGINGS)!!
        )
        crackedWitherLeggings.shape(
            "MRM",
            "N N",
            "R R"
        )
        crackedWitherLeggings.setIngredient('R', Material.WITHER_ROSE)
        crackedWitherLeggings.setIngredient('N', Material.NETHERITE_SCRAP)
        crackedWitherLeggings.setIngredient('M', Material.MAGMA_BLOCK)
        recipeRegistry.registerRecipe(crackedWitherLeggings)

        val crackedWitherBoots = ShapedRecipe(
            fromString(CRACKED_WITHER_BOOTS)!!,
            itemRegistry.getItemStack(CRACKED_WITHER_BOOTS)!!
        )
        crackedWitherBoots.shape(
            "R R",
            "M M",
            "R R"
        )
        crackedWitherBoots.setIngredient('R', Material.WITHER_ROSE)
        crackedWitherBoots.setIngredient('M', Material.MAGMA_BLOCK)
        recipeRegistry.registerRecipe(crackedWitherBoots)


        recipesToUnlock.add(CRACKED_WITHER_HELMET)
        recipesToUnlock.add(CRACKED_WITHER_CHESTPLATE)
        recipesToUnlock.add(CRACKED_WITHER_LEGGINGS)
        recipesToUnlock.add(CRACKED_WITHER_BOOTS)

    }

    private fun registerCustomDrops(itemRegistry: ItemRegistry) {
        itemRegistry.registerCustomDrops(
            CustomDrops.Builder(EntityType.WARDEN, itemRegistry)
                .drop(ECHO_CRYSTAL, 1.0, 1..4)
                .dropPredicate {
                    val directEntity = it.damageSource.directEntity
                    val causingEntity = it.damageSource.causingEntity

                    !(directEntity !is Player && (causingEntity !is Player || directEntity !is Projectile))
                }
                .build()
        )

        val witherAspectBook = ItemStack(Material.ENCHANTED_BOOK)
        val witherAspectBookMeta = witherAspectBook.itemMeta as EnchantmentStorageMeta
        witherAspectBookMeta.addStoredEnchant(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
            .get(fromString("crimson:wither_aspect")!!)!!, 1, false)
        witherAspectBook.itemMeta = witherAspectBookMeta


        itemRegistry.registerCustomDrops(
            CustomDrops.Builder(EntityType.WITHER, itemRegistry)
                .drop(witherAspectBook, 0.1, 1..1)
                .dropPredicate {
                    it.entity.scoreboardTags.contains("DropsWitherAspect")
                }
                .build()
        )
    }

    private fun registerMenus(kubLib: KubLib) {
        val itemRegistry = kubLib.itemRegistry
        val menuRegistry = kubLib.menuRegistry

        val raceSelectMenu = ChestMenu(
            translatable("menu.crimson.race_select.title", "Select a race", NamedTextColor.GOLD),
            "crimson:race_select_menu",
            "         ", " 123456  ", "         "
        )
        raceSelectMenu.setKey('1', raceSelectorMenuItem("human"))
        raceSelectMenu.setKey('2', raceSelectorMenuItem("golem"))
        raceSelectMenu.setKey('3', raceSelectorMenuItem("dwarf"))
        raceSelectMenu.setKey('4', raceSelectorMenuItem("drowned"))
        raceSelectMenu.setKey('5', raceSelectorMenuItem("demon"))
        raceSelectMenu.setKey('6', raceSelectorMenuItem("ender"))

        menuRegistry.registerMenu(raceSelectMenu)

        

        val raceConfirmMenu = ChestMenu(
            translatable("menu.crimson.race_confirm.title", "Confirm your selection", NamedTextColor.GOLD),
            "crimson:race_confirm_menu",
            "         ",
            "         ",
            "    r    ",
            "   y n   ",
            "         ",
            "         "
        )

        raceConfirmMenu.prepareKey('r', MenuItem(ItemStack.empty()) {}) {
            itemRegistry.getItemStack("crimson:race_selector_${selectedRaces[it.uniqueId]}")!!
        }

        raceConfirmMenu.setKey('y', MenuItem(ItemRegistry.createItem(
            Material.LIME_CONCRETE, translatable("menu.crimson.confirm", "Confirm", NamedTextColor.GREEN, TextDecoration.BOLD),
            listOf(translatable("menu.crimson.race_confirm.confirm.desc", "This action is permanent!", NamedTextColor.DARK_RED))
        )) {
            val player = it.event.whoClicked as Player
            val race = Race.valueOf(selectedRaces[player.uniqueId]!!.uppercase())
            setRaceType(player, race)
            player.sendMessage(
                translatable("message.race.select", "You have selected the %s race!", NamedTextColor.YELLOW, TextDecoration.BOLD)
                    .arguments(race.displayName.style(style(NamedTextColor.WHITE, TextDecoration.UNDERLINED, TextDecoration.BOLD)))
            )
            selectingPlayers.remove(player.uniqueId)
            player.isInvulnerable = false

            Bukkit.getScheduler().runTask(this, Runnable {
                player.closeInventory()
                player.teleport(race.spawnPos.get())
            })
        })

        raceConfirmMenu.setKey('n', MenuItem(ItemRegistry.createItem(
            Material.RED_CONCRETE, translatable("menu.crimson.cancel", "Cancel", NamedTextColor.DARK_RED, TextDecoration.BOLD)
        )) {
            selectedRaces.remove(it.event.whoClicked.uniqueId)
            Bukkit.getScheduler().runTask(this, Runnable {
                it.event.whoClicked.closeInventory()
                menuRegistry.openMenu("crimson:race_select_menu", it.event.whoClicked as Player)
            })
        })

        menuRegistry.registerMenu(raceConfirmMenu)



        val raceViewMenu = ChestMenu(
            translatable("menu.crimson.view_race.title", "View your race:", NamedTextColor.YELLOW),
            "crimson:view_race",
            "         ", "         ", "    i    ", "         ", "         "
        )
        raceViewMenu.prepareKey('i', MenuItem(ItemStack(Material.BARRIER)) {}) {
            val race = getRaceType(it)!!
            val level = getRaceLevel(it)
            val item = ItemStack(RACE_MATERIALS[race.name.lowercase()]!!)
            item.editMeta {
                it.displayName(race.displayName.append(text(" (Level $level)")))
                it.lore(race.abilityLores[level])
            }

            return@prepareKey item
        }

        menuRegistry.registerMenu(raceViewMenu)

    }

    private fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            val registrar = it.registrar()
            val crimsonCommand = Commands.literal("crimson")
                //.then(CrimsonHelpCommand().create())
                .then(CrimsonGiveCommand().create())
                .then(CrimsonBanhammerCommand().create())
                .then(CooldownCommand("crimson").createCommand())
                .then(CrimsonMenuCommand().create())
                .then(Commands.literal("reload")
                    .executes { context ->
                        reloadConfig()
                        context.source.sender.sendMessage("Configuration reloaded.")
                        1
                    }
                )
                .build()
            registrar.register(crimsonCommand)
            registrar.register(
                Commands.literal("c")
                    .redirect(crimsonCommand)
                    .build()
            )

            registrar.register(
                Commands.literal("race")
                    .then(RaceTypeCommand().create())
                    .then(RaceLevelCommand().create())
                    .then(RaceXpCommand().create())
                    .then(RaceViewCommand().create())
                    .then(RaceChangeCommand().create())
                    .build()
            )

            registrar.register(
                Commands.literal("demo")
                    .requires { it.sender.hasPermission("demo") }
                    .then(
                        Commands.argument("player", ArgumentTypes.player())
                            .executes { ctx ->
                                val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                                    .resolve(ctx.source).first()

                                player.showDemoScreen()
                                ctx.source.sender.sendMessage(
                                    "Showing the demo screen to ${player.name}"
                                )
                                1
                            }
                    )
                    .build()
            )

        }
    }


    private fun raceSelectorMenuItem(race: String): MenuItem {
        val kubLib = KubLib.getInstance()

        return MenuItem(
            kubLib.itemRegistry.getItemStack("crimson:race_selector_$race")!!
        ) {
            selectedRaces[it.event.whoClicked.uniqueId] = race
            Bukkit.getScheduler().runTask(this, Runnable {
                it.event.whoClicked.closeInventory()
                kubLib.menuRegistry.openMenu("crimson:race_confirm_menu", it.event.whoClicked as Player)
            })
        }
    }

    private fun translationString(itemId: String): String {
        return "item.${itemId.replace(':', '.')}"
    }

    fun savePluginConfig() {
        config.save(File(dataFolder, "config.yml"))
    }

    companion object {
        private var instance: Crimson? = null

        fun getInstance(): Crimson {
            return instance ?: throw IllegalStateException("Crimson is not initialized")
        }

        val PRISMARINE = TextColor.color(0x0e9e69)
        val ENDER = TextColor.color(0xaf00f5)

        const val ECHO_CRYSTAL = "crimson:echo_crystal"
        const val ECHO_INGOT = "crimson:echo_ingot"
        const val ECHO_UPGRADE = "crimson:echo_upgrade_smithing_template"

        const val ECHO_HELMET = "crimson:echo_helmet"
        const val ECHO_CHESTPLATE = "crimson:echo_chestplate"
        const val ECHO_LEGGINGS = "crimson:echo_leggings"
        const val ECHO_BOOTS = "crimson:echo_boots"

        const val PRISMARINE_ALLOY = "crimson:prismarine_alloy"

        const val ATLANTIS_HELMET = "crimson:atlantis_helmet"
        const val ATLANTIS_CHESTPLATE = "crimson:atlantis_chestplate"
        const val ATLANTIS_LEGGINGS = "crimson:atlantis_leggings"
        const val ATLANTIS_BOOTS = "crimson:atlantis_boots"

        const val ENDER_HELMET = "crimson:ender_helmet"
        const val ENDER_CHESTPLATE = "crimson:ender_chestplate"
        const val ENDER_LEGGINGS = "crimson:ender_leggings"
        const val ENDER_BOOTS = "crimson:ender_boots"

        const val DWARF_HELMET = "crimson:dwarf_helmet"
        const val DWARF_CHESTPLATE = "crimson:dwarf_chestplate"
        const val DWARF_LEGGINGS = "crimson:dwarf_leggings"
        const val DWARF_BOOTS = "crimson:dwarf_boots"

        const val WITHER_HELMET = "crimson:wither_helmet"
        const val WITHER_CHESTPLATE = "crimson:wither_chestplate"
        const val WITHER_LEGGINGS = "crimson:wither_leggings"
        const val WITHER_BOOTS = "crimson:wither_boots"

        const val CRACKED_WITHER_HELMET = "crimson:cracked_wither_helmet"
        const val CRACKED_WITHER_CHESTPLATE = "crimson:cracked_wither_chestplate"
        const val CRACKED_WITHER_LEGGINGS = "crimson:cracked_wither_leggings"
        const val CRACKED_WITHER_BOOTS = "crimson:cracked_wither_boots"
    }
}

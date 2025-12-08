@file:Suppress("UnstableApiUsage")

package me.kub94ek.crimson

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import me.kub94ek.crimson.command.crimson.CrimsonBanhammerCommand
import me.kub94ek.crimson.command.crimson.CrimsonGiveCommand
import me.kub94ek.crimson.command.crimson.CrimsonHelpCommand
import me.kub94ek.crimson.listeners.BanHammerListener
import me.kub94ek.kubLib.KubLib
import me.kub94ek.kubLib.cooldown.CooldownCommand
import me.kub94ek.kubLib.items.CustomDrops
import me.kub94ek.kubLib.items.ItemRegistry
import me.kub94ek.kubLib.items.effects.ArmorSetEffect
import me.kub94ek.kubLib.items.effects.SlotEffect
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey.fromString
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.SmithingTransformRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Crimson : JavaPlugin() {

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

        config.options().copyDefaults()
        saveDefaultConfig()

        val kubLib = KubLib.getInstance()

        registerListeners(kubLib)
        registerItems(kubLib)
        registerRecipes(kubLib)
        registerCustomDrops(kubLib.itemRegistry)
        registerCommands()

    }

    override fun onDisable() {
        logger.info("Shutting down...")
    }

    private fun registerListeners(kubLib: KubLib) {
        val pluginManager = server.pluginManager
        pluginManager.registerEvents(BanHammerListener(kubLib.itemRegistry), this)

    }

    private fun registerItems(kubLib: KubLib) {
        val itemRegistry = kubLib.itemRegistry


        val echoCrystal = itemRegistry.createItem(
            ECHO_CRYSTAL, Material.QUARTZ,
            translatable(translationString(ECHO_CRYSTAL), "Echo Crystal", NamedTextColor.DARK_AQUA)
        )
        echoCrystal.editMeta {
            it.setEnchantmentGlintOverride(true)
        }
        itemRegistry.registerItem(ECHO_CRYSTAL, echoCrystal)

        val echoIngot = itemRegistry.createItem(
            ECHO_INGOT, Material.NETHERITE_INGOT,
            translatable(translationString(ECHO_INGOT), "Echo Ingot", NamedTextColor.DARK_AQUA)
        )
        echoIngot.editMeta {
            it.setEnchantmentGlintOverride(true)
        }
        itemRegistry.registerItem(ECHO_INGOT, echoIngot)

        val echoUpgrade = itemRegistry.createItem(
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

        registerArmor(kubLib)
    }

    private fun registerArmor(kubLib: KubLib) {
        val itemRegistry = kubLib.itemRegistry

        val echoHelmet = itemRegistry.createItem(
            ECHO_HELMET, Material.NETHERITE_HELMET,
            translatable(translationString(ECHO_HELMET), "Echo Helmet", NamedTextColor.DARK_AQUA)
        )
        echoHelmet.editMeta {
            val equippable = it.equippable
            equippable.model = fromString("crimson:echo")
            equippable.slot = EquipmentSlot.HEAD
            it.setEquippable(equippable)
        }
        itemRegistry.registerItem(ECHO_HELMET, echoHelmet)

        val echoChestplate = itemRegistry.createItem(
            ECHO_CHESTPLATE, Material.NETHERITE_CHESTPLATE,
            translatable(translationString(ECHO_CHESTPLATE), "Echo Chestplate", NamedTextColor.DARK_AQUA)
        )
        echoChestplate.editMeta {
            val equippable = it.equippable
            equippable.model = fromString("crimson:echo")
            equippable.slot = EquipmentSlot.CHEST
            it.setEquippable(equippable)
        }
        itemRegistry.registerItem(ECHO_CHESTPLATE, echoChestplate)

        val echoLeggings = itemRegistry.createItem(
            ECHO_LEGGINGS, Material.NETHERITE_LEGGINGS,
            translatable(translationString(ECHO_LEGGINGS), "Echo Leggings", NamedTextColor.DARK_AQUA)
        )
        echoLeggings.editMeta {
            val equippable = it.equippable
            equippable.model = fromString("crimson:echo")
            equippable.slot = EquipmentSlot.LEGS
            it.setEquippable(equippable)
        }
        itemRegistry.registerItem(ECHO_LEGGINGS, echoLeggings)

        val echoBoots = itemRegistry.createItem(
            ECHO_BOOTS, Material.NETHERITE_BOOTS,
            translatable(translationString(ECHO_BOOTS), "Echo Boots", NamedTextColor.DARK_AQUA)
        )
        echoBoots.editMeta {
            val equippable = it.equippable
            equippable.model = fromString("crimson:echo")
            equippable.slot = EquipmentSlot.FEET
            it.setEquippable(equippable)
            it.addEnchant(Enchantment.MENDING, 1, false)
        }
        itemRegistry.registerItem(ECHO_BOOTS, echoBoots)


        val atlantisHelmet = itemRegistry.createItem(
            ATLANTIS_HELMET, Material.DIAMOND_HELMET,
            translatable(translationString(ATLANTIS_HELMET), "Atlantis Helmet", PRISMARINE)
        )
        atlantisHelmet.editMeta {
            val equippable = it.equippable
            equippable.model = fromString("crimson:atlantis")
            equippable.slot = EquipmentSlot.HEAD
            it.setEquippable(equippable)
        }
        itemRegistry.registerItem(ATLANTIS_HELMET, atlantisHelmet)

        val atlantisChestplate = itemRegistry.createItem(
            ATLANTIS_CHESTPLATE, Material.DIAMOND_CHESTPLATE,
            translatable(translationString(ATLANTIS_CHESTPLATE), "Atlantis Chestplate", PRISMARINE)
        )
        atlantisChestplate.editMeta {
            val equippable = it.equippable
            equippable.model = fromString("crimson:atlantis")
            equippable.slot = EquipmentSlot.CHEST
            it.setEquippable(equippable)
        }
        itemRegistry.registerItem(ATLANTIS_CHESTPLATE, atlantisChestplate)

        val atlantisLeggings = itemRegistry.createItem(
            ATLANTIS_LEGGINGS, Material.DIAMOND_LEGGINGS,
            translatable(translationString(ATLANTIS_LEGGINGS), "Atlantis Leggings", PRISMARINE)
        )
        atlantisLeggings.editMeta {
            val equippable = it.equippable
            equippable.model = fromString("crimson:atlantis")
            equippable.slot = EquipmentSlot.LEGS
            it.setEquippable(equippable)
        }
        itemRegistry.registerItem(ATLANTIS_LEGGINGS, atlantisLeggings)

        val atlantisBoots = itemRegistry.createItem(
            ATLANTIS_BOOTS, Material.DIAMOND_BOOTS,
            translatable(translationString(ATLANTIS_BOOTS), "Atlantis Boots", PRISMARINE)
        )
        atlantisBoots.editMeta {
            val equippable = it.equippable
            equippable.model = fromString("crimson:atlantis")
            equippable.slot = EquipmentSlot.FEET
            it.setEquippable(equippable)
        }
        itemRegistry.registerItem(ATLANTIS_BOOTS, atlantisBoots)

        itemRegistry.registerItemEffect(object : ArmorSetEffect(ATLANTIS_BOOTS, ATLANTIS_LEGGINGS, ATLANTIS_CHESTPLATE, ATLANTIS_HELMET, 20) {
            override fun applyEffect(player: Player) {
                player.addPotionEffect(PotionEffect(PotionEffectType.CONDUIT_POWER, 40, 0, true, false, true))
            }
        })

        itemRegistry.registerItemEffect(object : SlotEffect(ATLANTIS_BOOTS, 20, EquipmentSlot.FEET) {
            override fun applyEffect(player: Player) {
                player.addPotionEffect(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, true, false, true))
            }
        })

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


        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(ECHO_HELMET)!!,
            itemRegistry.getItemStack(ECHO_HELMET)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_UPGRADE)!!),
            RecipeChoice.MaterialChoice(Material.NETHERITE_HELMET),
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_INGOT)!!)
        ))

        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(ECHO_CHESTPLATE)!!,
            itemRegistry.getItemStack(ECHO_CHESTPLATE)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_UPGRADE)!!),
            RecipeChoice.MaterialChoice(Material.NETHERITE_CHESTPLATE),
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_INGOT)!!)
        ))

        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(ECHO_LEGGINGS)!!,
            itemRegistry.getItemStack(ECHO_LEGGINGS)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_UPGRADE)!!),
            RecipeChoice.MaterialChoice(Material.NETHERITE_LEGGINGS),
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_INGOT)!!)
        ))

        recipeRegistry.registerRecipe(SmithingTransformRecipe(
            fromString(ECHO_BOOTS)!!,
            itemRegistry.getItemStack(ECHO_BOOTS)!!,
            RecipeChoice.ExactChoice(itemRegistry.getItemStack(ECHO_UPGRADE)!!),
            RecipeChoice.MaterialChoice(Material.NETHERITE_BOOTS),
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
        recipeRegistry.registerRecipe(atlantisChestplateRecipe)

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
    }

    private fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            val registrar = it.registrar()
            val crimsonCommand = Commands.literal("crimson")
                .then(CrimsonHelpCommand().create())
                .then(CrimsonGiveCommand().create())
                .then(CrimsonBanhammerCommand().create())
                .then(CooldownCommand("crimson").createCommand())
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


    private fun translationString(itemId: String): String {
        return "item.${itemId.replace(':', '.')}"
    }

    companion object {
        private var instance: Crimson? = null

        fun getInstance(): Crimson {
            return instance ?: throw IllegalStateException("Crimson is not initialized")
        }

        val PRISMARINE = TextColor.color(0x0e9e69)

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

    }
}

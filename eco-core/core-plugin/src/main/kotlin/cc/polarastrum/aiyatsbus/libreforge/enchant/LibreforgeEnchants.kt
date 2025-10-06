@file:Suppress("UNUSED_PARAMETER", "DuplicatedCode")

package cc.polarastrum.aiyatsbus.libreforge.enchant

import cc.polarastrum.aiyatsbus.core.Aiyatsbus
import cc.polarastrum.aiyatsbus.core.registration.modern.ModernEnchantmentRegisterer
import cc.polarastrum.aiyatsbus.libreforge.AiyatsbusLibreforgePlugin
import cc.polarastrum.aiyatsbus.libreforge.enchant.impl.HardcodedLibreforgeAiyatsbusEnchant
import cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded.EnchantmentPermanenceCurse
import cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded.EnchantmentRepairing
import cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded.EnchantmentReplenish
import cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded.EnchantmentSoulbound
import cc.polarastrum.aiyatsbus.libreforge.feature.MigrateEcoEnchants
import cc.polarastrum.aiyatsbus.libreforge.plugin
import cc.polarastrum.aiyatsbus.libreforge.t
import com.willfp.eco.core.Prerequisite
import com.willfp.eco.core.config.Configs
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.RegistrableCategory
import java.io.File

/**
 * AiyatsbusLibreforge
 * com.mcstarrysky.aiyatsbus.libreforge.enchant.LibreforgeEnchants
 *
 * @author mical
 * @date 2024/8/21 19:47
 */
object LibreforgeEnchants : RegistrableCategory<LibreforgeAiyatsbusEnchant>("aiyatsbus_enchants", "enchantments") {

    private val hardcordedEnchants = listOf(
        "permanence_curse",
        "repairing",
        "replenish",
        "soulbound"
    )

    override fun beforeReload(plugin: LibreforgePlugin) {
        if (Prerequisite.HAS_1_20_3.isMet) {
            (Aiyatsbus.api().getEnchantmentRegisterer() as ModernEnchantmentRegisterer).unfreezeRegistry()
        }
    }

    override fun afterReload(plugin: LibreforgePlugin) {
        plugin as AiyatsbusLibreforgePlugin
        sendPrompts(plugin)
    }

    override fun acceptPreloadConfig(plugin: LibreforgePlugin, id: String, config: Config) = runWithCatching {
        if (Aiyatsbus.api().getEnchantmentManager().getEnchant(id) != null) {
            return@runWithCatching
        }
        val file = File(plugin.dataFolder, "enchantments")
            .walk()
            .firstOrNull { file -> file.nameWithoutExtension == id }!!

        if (!config.has("basic")) {
            MigrateEcoEnchants.migrate(file, id, config)
        }

        plugin as AiyatsbusLibreforgePlugin

        val newConfig = Configs.fromFile(file)
        try {
            if (id in hardcordedEnchants) {
                val enchant = when (id) {
                    "permanence_curse" -> EnchantmentPermanenceCurse(file, newConfig, plugin)
                    "repairing" -> EnchantmentRepairing(file, newConfig, plugin)
                    "replenish" -> EnchantmentReplenish(file, newConfig, plugin)
                    "soulbound" -> EnchantmentSoulbound(file, newConfig, plugin)
                    else -> error("Unexpected hardcorded enchantment id: $id")
                }
                doRegister(plugin, enchant)
                enchant.register()
            } else {
                if (!newConfig.has("effects")) {
                    return@runWithCatching
                }
                val enchant = LibreforgeAiyatsbusEnchant(
                    id,
                    file,
                    newConfig,
                    plugin
                )
                doRegister(plugin, enchant)
            }
        } catch (e: MissingDependencyException) {
            // Ignore missing dependencies for preloaded enchants
        } catch (e: Throwable) {
            plugin.logger.warning("Failed to loaded enchantment $id for reason: $e")
        }
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) = runWithCatching {
        if (Aiyatsbus.api().getEnchantmentManager().getEnchant(id) != null) {
            return@runWithCatching
        }
        val file = File(plugin.dataFolder, "enchantments")
            .walk()
            .firstOrNull { file -> file.nameWithoutExtension == id }!!

        if (!config.has("basic")) {
            MigrateEcoEnchants.migrate(file, id, config)
        }

        plugin as AiyatsbusLibreforgePlugin

        val newConfig = Configs.fromFile(file)
        try {
            if (id in hardcordedEnchants) {
                val enchant = when (id) {
                    "permanence_curse" -> EnchantmentPermanenceCurse(file, newConfig, plugin)
                    "repairing" -> EnchantmentRepairing(file, newConfig, plugin)
                    "replenish" -> EnchantmentReplenish(file, newConfig, plugin)
                    "soulbound" -> EnchantmentSoulbound(file, newConfig, plugin)
                    else -> error("Unexpected hardcorded enchantment id: $id")
                }
                doRegister(plugin, enchant)
                enchant.register()
            } else {
                if (!newConfig.has("effects")) {
                    return@runWithCatching
                }
                val enchant = LibreforgeAiyatsbusEnchant(
                    id,
                    file,
                    newConfig,
                    plugin
                )
                doRegister(plugin, enchant)
            }
        } catch (e: MissingDependencyException) {
            addPluginPrompt(plugin, e.plugins)
        } catch (e: Throwable) {
            plugin.logger.warning("Failed to loaded enchantment $id for reason: $e")
        }
    }

    override fun clear(plugin: LibreforgePlugin) {
        for (enchant in registry.values()) {
            if (enchant is HardcodedLibreforgeAiyatsbusEnchant) {
                enchant.remove()
            }
            Aiyatsbus.api().getEnchantmentManager().unregister(enchant)
        }
        registry.clear()
    }

    private fun doRegister(plugin: AiyatsbusLibreforgePlugin, enchant: LibreforgeAiyatsbusEnchant) {
        Aiyatsbus.api().getEnchantmentManager().register(enchant)
        registry.register(enchant)
    }

    private fun runWithCatching(func: () -> Unit) {
        try {
            func()
        } catch (e: Throwable) {
            if (e is MissingDependencyException) return
            plugin.logger.severe("""
                无法初始化附魔，为避免数据丢失，服务器将会被强制关闭！
                Failed to initialize enchantment. To avoid data loss, the server will be forced to shut down!
            """.t())
            e.printStackTrace()
            Thread.sleep(5000)
            Runtime.getRuntime().halt(-1)
        }
    }
}
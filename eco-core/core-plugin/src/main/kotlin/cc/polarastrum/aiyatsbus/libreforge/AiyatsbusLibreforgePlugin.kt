package cc.polarastrum.aiyatsbus.libreforge

import cc.polarastrum.aiyatsbus.core.Aiyatsbus
import cc.polarastrum.aiyatsbus.core.registration.modern.ModernEnchantmentRegisterer
import cc.polarastrum.aiyatsbus.libreforge.command.CommandAiyatsbusLibreforge
import cc.polarastrum.aiyatsbus.libreforge.enchant.LibreforgeEnchantLevel
import cc.polarastrum.aiyatsbus.libreforge.enchant.LibreforgeEnchants
import cc.polarastrum.aiyatsbus.libreforge.target.LibreforgeEnchantFinder
import cc.polarastrum.aiyatsbus.libreforge.target.LibreforgeEnchantFinder.clearEnchantmentCache
import cc.polarastrum.aiyatsbus.module.ingame.command.AiyatsbusCommand
import com.willfp.eco.core.Prerequisite
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.libreforge.NamedValue
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerHolderPlaceholderProvider
import com.willfp.libreforge.registerHolderProvider
import com.willfp.libreforge.registerSpecificRefreshFunction
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity
import java.io.File

/**
 * AiyatsbusLibreforgePlugin
 * com.mcstarrysky.aiyatsbus.libreforge.AiyatsbusLibreforgePlugin
 *
 * @author mical
 * @since 2024/7/20 22:30
 */
lateinit var plugin: AiyatsbusLibreforgePlugin
    private set

class AiyatsbusLibreforgePlugin : LibreforgePlugin() {

    var autoMigrate: Boolean

    init {
        plugin = this
        saveResource("config.yml", false)
//        val config = Configuration.loadFromFile(File(dataFolder, "config.yml"))
        val file = File(dataFolder, "config.yml")
        val config = YamlConfiguration.loadConfiguration(File(dataFolder, "config.yml"))
        autoMigrate = config.getBoolean("auto-migrate")
        if (autoMigrate) {
            config["auto-migrate"] = false
            config.save(file)
        }
    }

    override fun loadConfigCategories(): List<ConfigCategory> {
        // 在这之前, 先进行从 EcoEnchants 文件夹的复制操作
        if (autoMigrate) {
            val source = File(dataFolder.parent, "EcoEnchants/enchants/")
            val target = File(dataFolder, "enchantments/")

            source.walk().filter { it.isFile }.forEach { file ->
                val dest = File(target, file.relativeTo(source).path)
                if (!dest.exists()) {
                    dest.parentFile.mkdirs()
                    file.copyTo(dest)
                }
            }
        }
        return listOf(
            LibreforgeEnchants
        )
    }

    override fun handleEnable() {
        registerHolderProvider(LibreforgeEnchantFinder.toHolderProvider())

        registerSpecificRefreshFunction<LivingEntity> {
            it.clearEnchantmentCache()
        }

        registerHolderPlaceholderProvider<LibreforgeEnchantLevel> { it, _ ->
            listOf(
                NamedValue("level", it.level)
            )
        }
    }

    override fun handleAfterLoad() {
        if (Prerequisite.HAS_1_21.isMet) {
            (Aiyatsbus.api().getEnchantmentRegisterer() as ModernEnchantmentRegisterer).freezeRegistry()
        }

        AiyatsbusCommand.init()

        Bukkit.getConsoleSender().sendMessage(langYml.getMessage("loaded").replace("{counts}", LibreforgeEnchants.values().size.toString()))
    }

    override fun loadPluginCommands(): MutableList<PluginCommand> {
        return mutableListOf(
            CommandAiyatsbusLibreforge(this)
        )
    }
}
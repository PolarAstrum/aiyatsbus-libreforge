package cc.polarastrum.aiyatsbus.libreforge.enchant

import cc.polarastrum.aiyatsbus.core.AiyatsbusEnchantmentBase
import cc.polarastrum.aiyatsbus.core.data.trigger.Trigger
import cc.polarastrum.aiyatsbus.libreforge.AiyatsbusLibreforgePlugin
import cc.polarastrum.aiyatsbus.libreforge.plugin
import cc.polarastrum.aiyatsbus.taboolib.module.configuration.Configuration
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.KRegistrable
import com.willfp.eco.util.containsIgnoreCase
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.conditions.ConditionList
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.EffectList
import com.willfp.libreforge.effects.Effects
import org.bukkit.Bukkit
import java.io.File

/**
 * AiyatsbusLibreforge
 * com.mcstarrysky.aiyatsbus.libreforge.enchant.LibreforgeAiyatsbusEnchant
 *
 * @author mical
 * @date 2024/8/21 19:15
 */
open class LibreforgeAiyatsbusEnchant(
    id: String,
    file: File,
    ecoConfig: Config,
    plugin: AiyatsbusLibreforgePlugin
) : AiyatsbusEnchantmentBase(id, file, Configuration.loadFromFile(file)), KRegistrable {

    private val context = ViolationContext(plugin, "enchantment $id")

    internal val levels = mutableMapOf<Int, LibreforgeEnchantLevel>()

    override val trigger: Trigger? = null

    internal val conditions: ConditionList

    private val effects: EffectList

    open fun getLevel(level: Int): LibreforgeEnchantLevel {
        return levels.getOrPut(level) {
            LibreforgeEnchantLevel(this, level, effects, conditions, plugin)
        }
    }

    init {
        val missingPlugins = mutableSetOf<String>()

        for (dependency in ecoConfig.getStrings("dependencies")) {
            if (!Bukkit.getPluginManager().plugins.map { it.name }.containsIgnoreCase(dependency)) {
                missingPlugins += dependency
            }
        }

        if (missingPlugins.isNotEmpty()) {
            throw MissingDependencyException(missingPlugins)
        }

        // Compile here so MissingDependencyException is thrown before effects are compiled
        effects = Effects.compile(
            ecoConfig.getSubsections("effects"),
            context.with("effects")
        )

        conditions = Conditions.compile(
            ecoConfig.getSubsections("conditions"),
            context.with("conditions")
        )
    }
}
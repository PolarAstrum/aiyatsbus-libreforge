package cc.polarastrum.aiyatsbus.libreforge.enchant.impl

import cc.polarastrum.aiyatsbus.libreforge.AiyatsbusLibreforgePlugin
import cc.polarastrum.aiyatsbus.libreforge.enchant.LibreforgeAiyatsbusEnchant
import cc.polarastrum.aiyatsbus.libreforge.enchant.LibreforgeEnchantLevel
import cc.polarastrum.aiyatsbus.libreforge.plugin
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.effects.emptyEffectList
import java.io.File
import kotlin.collections.getOrPut

/**
 * AiyatsbusLibreforge
 * cc.polarastrum.aiyatsbus.libreforge.enchant.impl.HardcodedLibreforgeAiyatsbusEnchant
 *
 * @author mical
 * @since 2025/10/5 20:42
 */
abstract class HardcodedLibreforgeAiyatsbusEnchant(
    id: String,
    file: File,
    ecoConfig: Config,
    plugin: AiyatsbusLibreforgePlugin
) : LibreforgeAiyatsbusEnchant(id, file, ecoConfig, plugin) {

    override fun getLevel(level: Int): LibreforgeEnchantLevel {
        return levels.getOrPut(level) {
            LibreforgeEnchantLevel(this, level, emptyEffectList(), conditions, plugin)
        }
    }

    open fun register() {
    }

    open fun remove() {
    }
}
package cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded

import cc.polarastrum.aiyatsbus.core.aiyatsbusEt
import cc.polarastrum.aiyatsbus.core.etLevel
import cc.polarastrum.aiyatsbus.core.event.AiyatsbusPrepareAnvilEvent
import cc.polarastrum.aiyatsbus.libreforge.AiyatsbusLibreforgePlugin
import cc.polarastrum.aiyatsbus.libreforge.enchant.impl.HardcodedLibreforgeAiyatsbusEnchant
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.items.isEmpty
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File

/**
 * AiyatsbusLibreforge
 * cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded.EnchantmentPermanenceCurse
 *
 * @author mical
 * @since 2025/10/5 21:51
 */
class EnchantmentPermanenceCurse(
    file: File,
    ecoConfig: Config,
    val plugin: AiyatsbusLibreforgePlugin
) : HardcodedLibreforgeAiyatsbusEnchant(
    "permanence_curse",
    file,
    ecoConfig,
    plugin
) {

    private val handler = PermanenceCurseHandler(this, plugin)

    override fun register() {
        plugin.eventManager.registerListener(handler)
    }

    override fun remove() {
        plugin.eventManager.unregisterListener(handler)
    }

    private class PermanenceCurseHandler(
        private val enchant: EnchantmentPermanenceCurse,
        private val plugin: AiyatsbusLibreforgePlugin
    ) : Listener {

        @EventHandler
        fun handle(event: AiyatsbusPrepareAnvilEvent) {
            val first = event.left
            if (first.isEmpty || first.type.isAir) {
                return
            }
            if (first.etLevel(aiyatsbusEt("permanence_curse")!!) >= 1) {
                event.isCancelled = true
            }
        }
    }
}
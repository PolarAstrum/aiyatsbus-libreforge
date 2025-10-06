package cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded

import cc.polarastrum.aiyatsbus.libreforge.AiyatsbusLibreforgePlugin
import cc.polarastrum.aiyatsbus.libreforge.enchant.impl.HardcodedLibreforgeAiyatsbusEnchant
import cc.polarastrum.aiyatsbus.libreforge.target.LibreforgeEnchantFinder.getItemsWithEnchantActive
import cc.polarastrum.aiyatsbus.libreforge.target.LibreforgeEnchantFinder.hasEnchantActive
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.util.DurabilityUtils
import com.willfp.libreforge.slot.impl.SlotTypeHands
import org.bukkit.Bukkit
import java.io.File

/**
 * AiyatsbusLibreforge
 * cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded.EnchantmentRepairing
 *
 * @author mical
 * @since 2025/10/5 21:28
 */
class EnchantmentRepairing(
    file: File,
    val ecoConfig: Config,
    val plugin: AiyatsbusLibreforgePlugin
) : HardcodedLibreforgeAiyatsbusEnchant(
    "repairing",
    file,
    ecoConfig,
    plugin
) {

    override fun register() {
        val frequency = ecoConfig.getInt("frequency").toLong()

        plugin.scheduler.runTimer(frequency, frequency) {
            handleRepairing()
        }
    }

    private fun handleRepairing() {
        val notWhileHolding = ecoConfig.getBool("not-while-holding")

        for (player in Bukkit.getOnlinePlayers()) {
            if (player.hasEnchantActive(this)) {
                val repairPerLevel = ecoConfig.getIntFromExpression("repair-per-level", player)

                for ((item, level) in player.getItemsWithEnchantActive(this)) {
                    val isHolding = item in SlotTypeHands.getItems(player)

                    if (notWhileHolding && isHolding) {
                        continue
                    }

                    DurabilityUtils.repairItem(item, level * repairPerLevel)
                }
            }
        }
    }
}
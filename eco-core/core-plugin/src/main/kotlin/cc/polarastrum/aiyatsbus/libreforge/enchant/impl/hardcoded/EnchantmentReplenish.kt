package cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded

import cc.polarastrum.aiyatsbus.libreforge.AiyatsbusLibreforgePlugin
import cc.polarastrum.aiyatsbus.libreforge.enchant.impl.HardcodedLibreforgeAiyatsbusEnchant
import cc.polarastrum.aiyatsbus.libreforge.target.LibreforgeEnchantFinder.hasEnchantActive
import com.willfp.eco.core.config.interfaces.Config
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.io.File

/**
 * AiyatsbusLibreforge
 * cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded.EnchantmentReplenish
 *
 * @author mical
 * @since 2025/10/5 21:36
 */
class EnchantmentReplenish(
    file: File,
    val ecoConfig: Config,
    val plugin: AiyatsbusLibreforgePlugin
) : HardcodedLibreforgeAiyatsbusEnchant(
    "replenish",
    file,
    ecoConfig,
    plugin
) {
    private var handler = ReplenishHandler(this, plugin)

    override fun register() {
        plugin.eventManager.registerListener(handler)
    }

    override fun remove() {
        plugin.eventManager.unregisterListener(handler)
    }

    private class ReplenishHandler(
        private val enchant: EnchantmentReplenish,
        private val plugin: AiyatsbusLibreforgePlugin
    ) : Listener {
        @EventHandler(
            ignoreCancelled = true
        )
        fun handle(event: BlockBreakEvent) {
            val player = event.player

            if (!player.hasEnchantActive(enchant)) {
                return
            }

            val block = event.block
            val type = block.type

            if (type in arrayOf(
                    Material.GLOW_BERRIES,
                    Material.SWEET_BERRY_BUSH,
                    Material.CACTUS,
                    Material.BAMBOO,
                    Material.CHORUS_FLOWER,
                    Material.SUGAR_CANE
                )
            ) {
                return
            }

            val data = block.blockData

            if (data !is Ageable) {
                return
            }

            if (enchant.ecoConfig.getBool("consume-seeds")) {
                val item = ItemStack(
                    when (type) {
                        Material.WHEAT -> Material.WHEAT_SEEDS
                        Material.POTATOES -> Material.POTATO
                        Material.CARROTS -> Material.CARROT
                        Material.BEETROOTS -> Material.BEETROOT_SEEDS
                        Material.COCOA -> Material.COCOA_BEANS
                        else -> type
                    }
                )

                val hasSeeds = player.inventory.removeItem(item).isEmpty()

                if (!hasSeeds) {
                    return
                }
            }

            if (data.age != data.maximumAge) {
                if (enchant.ecoConfig.getBool("only-fully-grown")) {
                    return
                }

                event.isDropItems = false
                event.expToDrop = 0
            }

            data.age = 0

            plugin.scheduler.run {
                block.type = type
                block.blockData = data

                // Improves compatibility with other plugins.
                Bukkit.getPluginManager().callEvent(
                    BlockPlaceEvent(
                        block,
                        block.state,
                        block.getRelative(BlockFace.DOWN),
                        player.inventory.itemInMainHand,
                        player,
                        true,
                        EquipmentSlot.HAND
                    )
                )
            }
        }
    }
}
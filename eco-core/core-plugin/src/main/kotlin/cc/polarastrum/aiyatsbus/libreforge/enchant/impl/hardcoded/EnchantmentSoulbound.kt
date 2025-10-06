package cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded

import cc.polarastrum.aiyatsbus.libreforge.AiyatsbusLibreforgePlugin
import cc.polarastrum.aiyatsbus.libreforge.enchant.impl.HardcodedLibreforgeAiyatsbusEnchant
import cc.polarastrum.aiyatsbus.libreforge.target.LibreforgeEnchantFinder.getItemsWithEnchantActive
import com.willfp.eco.core.Prerequisite
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.drops.DropQueue
import com.willfp.eco.core.fast.fast
import com.willfp.eco.core.items.Items
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.persistence.PersistentDataType
import java.io.File
import kotlin.collections.emptyList

/**
 * AiyatsbusLibreforge
 * cc.polarastrum.aiyatsbus.libreforge.enchant.impl.hardcoded.EnchantmentSoulbound
 *
 * @author mical
 * @since 2025/10/5 21:40
 */
class EnchantmentSoulbound(
    file: File,
    val ecoConfig: Config,
    val plugin: AiyatsbusLibreforgePlugin
) : HardcodedLibreforgeAiyatsbusEnchant(
    "soulbound",
    file,
    ecoConfig,
    plugin
) {

    private val handler = SoulboundHandler(plugin, this)

    override fun register() {
        plugin.eventManager.registerListener(handler)
    }

    override fun remove() {
        plugin.eventManager.unregisterListener(handler)
    }

    private class SoulboundHandler(
        private val plugin: AiyatsbusLibreforgePlugin,
        private val enchant: EnchantmentSoulbound
    ) : Listener {
        private val savedSoulboundItems = PersistentDataKey(
            plugin.namespacedKeyFactory.create("soulbound_items"),
            PersistentDataKeyType.STRING_LIST,
            emptyList()
        )

        private val soulboundKey = plugin.namespacedKeyFactory.create("soulbound")

        @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
        )
        fun handle(event: PlayerDeathEvent) {
            if (event.keepInventory) {
                return
            }

            val player = event.entity
            val items = player.getItemsWithEnchantActive(enchant).keys

            if (items.isEmpty()) {
                return
            }

            event.drops.removeAll(items)

            // Use native paper method
            if (Prerequisite.HAS_PAPER.isMet) {
                val modifiedItems = if (enchant.ecoConfig.getBool("single-use")) {
                    items.map {
                        val meta = it.itemMeta
                        meta.removeEnchant(enchant.enchantment)
                        it.itemMeta = meta
                        it
                    }
                } else {
                    items
                }

                event.itemsToKeep += modifiedItems
                return
            }

            for (item in items) {
                item.fast().persistentDataContainer.set(soulboundKey, PersistentDataType.INTEGER, 1)

                if (enchant.ecoConfig.getBool("single-use")) {
                    val meta = item.itemMeta
                    meta.removeEnchant(enchant.enchantment)
                    item.itemMeta = meta
                }
            }

            player.profile.write(savedSoulboundItems, items.map { Items.toSNBT(it) })
        }

        @EventHandler(
            ignoreCancelled = true
        )
        fun onJoin(event: PlayerJoinEvent) {
            giveItems(event.player)
        }

        @EventHandler(
            ignoreCancelled = true
        )
        fun onJoin(event: PlayerRespawnEvent) {
            giveItems(event.player)
        }

        private fun giveItems(player: Player) {
            val itemStrings = player.profile.read(savedSoulboundItems)

            if (itemStrings.isEmpty()) {
                return
            }

            val items = itemStrings.map { Items.fromSNBT(it) }

            plugin.scheduler.run {
                DropQueue(player)
                    .addItems(items)
                    .forceTelekinesis()
                    .push()
            }

            player.profile.write(savedSoulboundItems, emptyList())
        }

        @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
        )
        fun preventDroppingSoulboundItems(event: PlayerDeathEvent) {
            event.drops.removeIf {
                it.fast().persistentDataContainer.has(soulboundKey, PersistentDataType.INTEGER)
                        && it.itemMeta.hasEnchant(enchant.enchantment)
            }
        }
    }
}
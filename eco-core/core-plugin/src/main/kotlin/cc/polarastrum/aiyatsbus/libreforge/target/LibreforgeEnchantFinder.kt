package cc.polarastrum.aiyatsbus.libreforge.target

import cc.polarastrum.aiyatsbus.core.fixedEnchants
import cc.polarastrum.aiyatsbus.libreforge.enchant.LibreforgeAiyatsbusEnchant
import cc.polarastrum.aiyatsbus.libreforge.enchant.LibreforgeEnchantLevel
import cc.polarastrum.aiyatsbus.libreforge.enchant.LibreforgeEnchants
import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.libreforge.*
import com.willfp.libreforge.slot.ItemHolderFinder
import com.willfp.libreforge.slot.SlotType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * AiyatsbusLibreforge
 * com.mcstarrysky.aiyatsbus.libreforge.LibreforgeEnchantFinder
 *
 * @author mical
 * @date 2024/8/21 20:08
 */
object LibreforgeEnchantFinder : ItemHolderFinder<LibreforgeEnchantLevel>() {

    private val transfer = mapOf(
        "armor" to listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET),
        "boots" to listOf(EquipmentSlot.FEET),
        "chestplate" to listOf(EquipmentSlot.CHEST),
        "hand" to listOf(EquipmentSlot.HAND),
        "hands" to listOf(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND),
        "helmet" to listOf(EquipmentSlot.HEAD),
        "leggings" to listOf(EquipmentSlot.LEGS),
        "mainhand" to listOf(EquipmentSlot.HAND),
        "offhand" to listOf(EquipmentSlot.OFF_HAND),
        "any" to EquipmentSlot.entries,
    )

    private val levelCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.SECONDS)
        .build<UUID, List<ProvidedLevel>>()

    override fun find(item: ItemStack): List<LibreforgeEnchantLevel> {
        val enchantMap = item.fixedEnchants
        val enchants = mutableListOf<LibreforgeEnchantLevel>()

        for (enchant in enchantMap) {
            val libreforgeEnchant = LibreforgeEnchants[enchant.key.id] ?: continue

            enchants += libreforgeEnchant.getLevel(enchant.value)
        }

        return enchants
    }

    override fun isValidInSlot(holder: LibreforgeEnchantLevel, slot: SlotType): Boolean {
        return holder.enchant.targets.map { it.activeSlots }
            .any { target ->
                transfer[slot.id]?.any { it in target } == true
//                it == SlotTypeTransfer.transfer[slot.id]
            }
    }

    internal fun LivingEntity.clearEnchantmentCache() = levelCache.invalidate(this.uniqueId)

    private val LivingEntity.cachedLevels: List<ProvidedLevel>
        get() = levelCache.get(this.uniqueId) {
            toHolderProvider().provide(this.toDispatcher())
                .mapNotNull {
                    val level = it.holder as? LibreforgeEnchantLevel ?: return@mapNotNull null
                    val item = it.provider as? ItemStack ?: return@mapNotNull null

                    ProvidedLevel(level, item, it)
                }
        }

    fun LivingEntity.hasEnchantActive(enchant: LibreforgeAiyatsbusEnchant): Boolean {
        return this.cachedLevels
            .filter { it.level.enchant == enchant }
            .any { it.level.conditions.areMet(this.toDispatcher(), it.holder) }
    }

    fun LivingEntity.getItemsWithEnchantActive(enchant: LibreforgeAiyatsbusEnchant): Map<ItemStack, Int> {
        return this.cachedLevels
            .filter { it.level.enchant == enchant }
            .filter { it.level.conditions.areMet(this.toDispatcher(), it.holder) }
            .associate { it.item to it.level.level }
    }

    private data class ProvidedLevel(
        val level: LibreforgeEnchantLevel,
        val item: ItemStack,
        val holder: ProvidedHolder
    )
}
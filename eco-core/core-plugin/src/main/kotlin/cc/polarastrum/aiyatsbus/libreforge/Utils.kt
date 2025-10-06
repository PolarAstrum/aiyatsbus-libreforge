package cc.polarastrum.aiyatsbus.libreforge

import cc.polarastrum.aiyatsbus.taboolib.common.PrimitiveIO

/**
 * AiyatsbusLibreforge
 * cc.polarastrum.aiyatsbus.libreforge.Utils
 *
 * @author mical
 * @since 2025/10/6 00:57
 */

/**
 * 针对中文环境进行特殊适配，以支持在中文环境中输出本土化的提示信息。
 * 其他语言环境均输出英文。
 *
 * 使用方式为：
 * ```
 * warning(
 *     """
 *     当前 Minecraft 版本不受支持，请等待插件适配。
 *     The current Minecraft version is not supported, please wait for the plugin to be adapted.
 *     """.t()
 * )
 * ```
 *
 * 这样的设计有两个原因：
 * 1. 不影响 IDEA 格式化，保持代码的可读性。
 * 2. 不依赖语言文件，仅用于 TabooLib 内部的提示信息。
 */
fun String.t(): String {
    val lines = trimIndent().lines()
    return if (PrimitiveIO.isChineseEnvironment()) lines.first() else lines.last()
}
package org.javamaster.mybatis.tools.utils

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId

/**
 * @author yudong
 */
object DatabasePluginUtils {

    val databasePluginAvailable by lazy {
        val pluginId = PluginId.findId("com.intellij.database") ?: return@lazy false
        val plugin = PluginManager.getInstance().findEnabledPlugin(pluginId)
        plugin != null
    }

}

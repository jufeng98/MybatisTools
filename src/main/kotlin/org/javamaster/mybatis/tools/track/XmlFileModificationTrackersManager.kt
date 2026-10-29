package org.javamaster.mybatis.tools.track

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.jetbrains.rd.util.concurrentMapOf

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class XmlFileModificationTrackersManager {
    private val modificationTrackerMap = concurrentMapOf<String, ModuleXmlFileModificationTracker>()

    fun getModificationTracker(holder: UserDataHolder): ModuleXmlFileModificationTracker {
        val name = getName(holder)

        return modificationTrackerMap.computeIfAbsent(name) {
            ModuleXmlFileModificationTracker()
        }
    }

    fun getName(holder: UserDataHolder): String {
        return if (holder is Module) {
            holder.name
        } else {
            (holder as Project).name
        }
    }

}

package org.javamaster.mybatis.tools.track

import com.intellij.openapi.components.Service

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class XmlFileModificationTrackersManager {
    private val modificationTrackersManager = ModuleXmlFileModificationTracker()

    fun getModificationTracker(): ModuleXmlFileModificationTracker {
        return modificationTrackersManager
    }

}

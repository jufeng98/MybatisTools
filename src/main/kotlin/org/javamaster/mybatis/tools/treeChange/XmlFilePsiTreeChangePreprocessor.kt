package org.javamaster.mybatis.tools.treeChange

import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import com.intellij.psi.xml.XmlFile
import org.javamaster.mybatis.tools.track.XmlFileModificationTrackersManager

/**
 * @author yudong
 */
class XmlFilePsiTreeChangePreprocessor : PsiTreeChangePreprocessor {

    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        val xmlFile = event.file as? XmlFile ?: return

        val code = event.code
        if (code != PsiTreeChangeEventImpl.PsiEventType.CHILDREN_CHANGED) return

        val project = xmlFile.project

        val trackersManager = project.getService(XmlFileModificationTrackersManager::class.java)

        val modificationTracker = trackersManager.getModificationTracker()

        modificationTracker.incModificationCount()
    }

}
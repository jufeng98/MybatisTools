package org.javamaster.mybatis.tools.service

import com.baomidou.plugin.idea.mybatisx.dom.converter.IdBasedTagConverter
import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.jetbrains.rd.util.concurrentMapOf
import org.javamaster.mybatis.tools.track.XmlFileModificationTrackersManager
import java.util.*

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class IdDomElementService(val project: Project) {

    fun getCacheDomElementMap(
        module: Module?,
        idBasedTagConverter: IdBasedTagConverter,
    ): Map<String, MutableCollection<out IdDomElement>> {
        val trackersManager = project.getService(XmlFileModificationTrackersManager::class.java)

        val holder = module ?: project

        val key = trackersManager.getName(holder) + "." + idBasedTagConverter.javaClass.simpleName

        val cachedValueKey = domElementKeyMap.computeIfAbsent(key) {
            Key.create("mybatisTools.domElementKeyMap.$it")
        }

        return CachedValuesManager.getManager(project)
            .getCachedValue(
                holder, cachedValueKey, CachedValueProvider { this.create(module, idBasedTagConverter) }, false
            )
    }

    private fun create(
        module: Module?,
        idBasedTagConverter: IdBasedTagConverter,
    ): CachedValueProvider.Result<Map<String, MutableCollection<out IdDomElement>>> {
        val holder = module ?: project

        val trackersManager = project.getService(XmlFileModificationTrackersManager::class.java)
        val modificationTracker = trackersManager.getModificationTracker(holder)

        val map: MutableMap<String, MutableCollection<out IdDomElement>> = mutableMapOf()

        val mappers = MapperUtils.findMappers(module, project)
        for (mapper in mappers) {
            val domMap = idBasedTagConverter.getComparisons(mapper)

            map.putAll(domMap)
        }

        return CachedValueProvider.Result.create(Collections.unmodifiableMap(map), modificationTracker)
    }

    companion object {
        val domElementKeyMap =
            concurrentMapOf<String, Key<CachedValue<Map<String, MutableCollection<out IdDomElement>>>>>()
    }
}

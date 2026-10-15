package org.javamaster.mybatis.tools.sem

import com.intellij.jam.reflect.JamAnnotationMeta
import com.intellij.jam.reflect.JamClassMeta
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElementRef
import com.intellij.semantic.SemKey
import com.intellij.spring.model.CommonSpringBean
import com.intellij.spring.model.extensions.myBatis.SpringMyBatisBeansProvider
import com.intellij.spring.model.jam.converters.SpringAntPatternPackageReferenceSet
import com.intellij.spring.model.jam.stereotype.SpringJamComponentScanArchetype
import com.intellij.spring.search.SpringGlobalSearchScopes

/**
 * @author yudong
 */
class SpringTkMyBatisMapperScanJamComponent : SpringJamComponentScanArchetype {
    constructor(psiRef: PsiElementRef<*>) : super(psiRef)

    @Suppress("unused")
    constructor(psiAnnotation: PsiAnnotation) : super(psiAnnotation)

    override fun getAnnotationMeta(): JamAnnotationMeta {
        return annoMeta
    }

    override fun getScannedBeans(module: Module, includeTests: Boolean): Set<CommonSpringBean> {
        val psiAnnotation = annotationRef.psiElement ?: return emptySet()

        val mapperScanModule = ModuleUtilCore.findModuleForPsiElement(psiAnnotation) ?: return emptySet()

        val mapperBeans = mutableSetOf<CommonSpringBean>()

        val searchScope = SpringGlobalSearchScopes.runtime(module, includeTests)

        if (module == mapperScanModule) {
            for (mapperPackage in psiPackages) {
                SpringMyBatisBeansProvider.processBasePackage(searchScope, mapperPackage, mapperBeans)
            }
        } else {
            // 处理当 MapperScan 注解不在当前模块的情况
            val moduleManager = ModuleManager.getInstance(module.project)

            if (moduleManager.isModuleDependent(module, mapperScanModule)) {
                packageJamAttributes.forEach { attrMeta ->
                    attrMeta.getJam(annotationRef).forEach {
                        val stringValue = it.stringValue ?: return@forEach

                        val element = it.psiElement ?: return@forEach

                        val referenceSet = SpringAntPatternPackageReferenceSet(stringValue, element, 1, searchScope)

                        val packages = referenceSet.resolvePackage()

                        for (mapperPackage in packages) {
                            SpringMyBatisBeansProvider.processBasePackage(searchScope, mapperPackage, mapperBeans)
                        }
                    }
                }
            }
        }

        return mapperBeans
    }

    companion object {
        const val TK_MAPPER_SCAN_ANNO = "tk.mybatis.spring.annotation.MapperScan"

        private val annoMeta: JamAnnotationMeta = JamAnnotationMeta(TK_MAPPER_SCAN_ANNO, ARCHETYPE, META_KEY)

        private val semKey: SemKey<SpringTkMyBatisMapperScanJamComponent> =
            COMPONENT_SCAN_JAM_KEY.subKey("TkMybatisComponentScanJamComponent")

        val jamClassMeta: JamClassMeta<SpringTkMyBatisMapperScanJamComponent> = JamClassMeta(
            null, { psiRef -> SpringTkMyBatisMapperScanJamComponent(psiRef) }, semKey
        ).addAnnotation(annoMeta)
    }
}

package org.javamaster.mybatis.tools.utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlText
import org.apache.commons.lang3.StringUtils

/**
 * @author yudong
 */
object PsiUtils {

    fun resolvePsiType(psiType: PsiType?): PsiClass? {
        if (psiType !is PsiClassReferenceType) {
            return null
        }

        return psiType.resolve()
    }

    fun resolvePsiClass(fullClassName: String, module: Module?, project: Project): PsiClass? {
        val scope = if (module != null) {
            GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
        } else {
            GlobalSearchScope.everythingScope(project)
        }

        val javaPsiFacade = JavaPsiFacade.getInstance(project)

        return javaPsiFacade.findClass(fullClassName, scope)
    }

    fun getNextNotBlankXmlText(xmlTexts: Collection<XmlText>, start: Int): XmlText? {
        var idx = start
        while (true) {
            var next = xmlTexts.elementAtOrNull(idx)

            if (next == null) {
                return null
            }

            val value = next.value
            if (StringUtils.isNotBlank(value)) {
                return next
            }

            idx++
        }
    }

}

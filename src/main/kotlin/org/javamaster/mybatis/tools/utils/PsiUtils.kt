package org.javamaster.mybatis.tools.utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import org.javamaster.mybatis.tools.psi.impl.MapValueFakePsiElement
import java.util.*

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

    fun resolveTarget(
        currentPsiElement: PsiElement,
        paramPsiCls: PsiClass,
        nameLevels: LinkedList<String>,
    ): PsiElement? {
        var psiField: PsiField? = null

        try {
            var fieldTypeCls = paramPsiCls
            var propertyName = nameLevels.pop()

            while (true) {
                if (InheritanceUtil.isInheritor(fieldTypeCls, "java.util.Map")) {
                    return MapValueFakePsiElement(currentPsiElement, fieldTypeCls)
                }

                psiField = fieldTypeCls.findFieldByName(propertyName, true)
                if (psiField == null) {
                    val psiMethods = fieldTypeCls.findMethodsByName(propertyName, true)
                    if (psiMethods.isNotEmpty()) {
                        return psiMethods[0]
                    }

                    return null
                }

                if (psiField.type !is PsiClassType) {
                    return psiField
                }

                val psiType = psiField.type as PsiClassType

                fieldTypeCls = resolvePsiType(psiType) ?: return null

                propertyName = nameLevels.pop()
            }
        } catch (_: NoSuchElementException) {
        }

        return psiField
    }
}

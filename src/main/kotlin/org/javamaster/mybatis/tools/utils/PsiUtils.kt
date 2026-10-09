package org.javamaster.mybatis.tools.utils

import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import java.util.*

object PsiUtils {

    fun resolvePsiType(psiType: PsiType?): PsiClass? {
        if (psiType !is PsiClassReferenceType) {
            return null
        }

        return psiType.resolve()
    }

    fun resolveTarget(
        paramPsiCls: PsiClass,
        nameLevels: LinkedList<String>,
    ): PsiElement? {
        var psiField: PsiField? = null

        try {
            var fieldTypeCls = paramPsiCls
            var propertyName = nameLevels.pop()

            while (true) {
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

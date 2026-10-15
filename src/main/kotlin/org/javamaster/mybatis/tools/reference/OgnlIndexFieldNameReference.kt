package org.javamaster.mybatis.tools.reference

import com.intellij.lang.ognl.psi.OgnlIndexedExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.javamaster.mybatis.tools.utils.PsiUtils
import org.javamaster.mybatis.tools.utils.ReferenceUtils.resolveTarget
import java.util.*

/**
 * @author yudong
 */
class OgnlIndexFieldNameReference(
    indexedExpression: OgnlIndexedExpression,
    val from: TextRange,
    private val nameLevels: LinkedList<String>,
) : PsiReferenceBase<OgnlIndexedExpression>(indexedExpression, from) {

    override fun resolve(): PsiElement? {
        val referenceQualifier = myElement.referenceQualifier
        val psiReferences = referenceQualifier.references
        if (psiReferences.isEmpty()) {
            return null
        }

        val resolve = psiReferences.last().resolve()

        val genericPsiClass = if (resolve is PsiParameter || resolve is PsiField) {
            val genericParameters = (resolve.type as PsiClassReferenceType).parameters
            if (genericParameters.isEmpty()) {
                null
            } else {
                PsiUtils.resolvePsiType(genericParameters[0]) ?: return null
            }
        } else {
            null
        } ?: return null


        return resolveTarget(myElement, genericPsiClass, nameLevels)
    }

}
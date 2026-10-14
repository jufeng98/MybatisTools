package org.javamaster.mybatis.tools.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase

/**
 * @author yudong
 */
class MybatisStaticClassReference(
    psiElement: PsiElement,
    from: TextRange,
    val reference: PsiReference,
) : PsiReferenceBase<PsiElement>(psiElement, from) {

    override fun resolve(): PsiElement? {
        return reference.resolve()
    }

}
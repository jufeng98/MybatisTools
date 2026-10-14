package org.javamaster.mybatis.tools.reference

import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.mybatis.tools.utils.PsiUtils

/**
 * @author yudong
 */
class MybatisStaticClassReference(
    psiElement: PsiElement,
    val from: TextRange,
    val fullClassName: String,
) : PsiReferenceBase<PsiElement>(psiElement, from) {

    override fun resolve(): PsiElement? {
        val module = ModuleUtil.findModuleForPsiElement(myElement)

        return PsiUtils.resolvePsiClass(fullClassName, module, myElement.project)
    }

}
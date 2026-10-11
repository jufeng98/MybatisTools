package org.javamaster.mybatis.tools.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope

/**
 * @author yudong
 */
class MybatisStaticClassReference(
    psiElement: PsiElement,
    val from: TextRange,
    val fullClassName: String,
) : PsiReferenceBase<PsiElement>(psiElement, from) {

    override fun resolve(): PsiElement? {
        val project = myElement.project
        return JavaPsiFacade.getInstance(project).findClass(fullClassName, GlobalSearchScope.everythingScope(project))
    }

}
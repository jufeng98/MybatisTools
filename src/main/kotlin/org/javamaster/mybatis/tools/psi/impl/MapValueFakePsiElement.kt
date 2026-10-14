package org.javamaster.mybatis.tools.psi.impl

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement

/**
 * @author yudong
 */
class MapValueFakePsiElement(val currentPsiElement: PsiElement, val psiClass: PsiClass) : FakePsiElement() {

    override fun canNavigate(): Boolean {
        return false
    }

    override fun getName(): String {
        return currentPsiElement.text
    }

    override fun getParent(): PsiElement {
        return psiClass
    }

}

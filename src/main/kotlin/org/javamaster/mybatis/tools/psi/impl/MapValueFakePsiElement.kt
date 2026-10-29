package org.javamaster.mybatis.tools.psi.impl

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement

/**
 * @author yudong
 */
class MapValueFakePsiElement(val currentPsiElement: PsiElement, val psiClass: PsiClass) : FakePsiElement() {

    override fun canNavigate(): Boolean {
        return psiClass.canNavigate()
    }

    override fun navigate(requestFocus: Boolean) {
        psiClass.navigate(requestFocus)
    }

    override fun canNavigateToSource(): Boolean {
        return psiClass.canNavigateToSource()
    }

    override fun getName(): String? {
        return currentPsiElement.text
    }

    override fun getParent(): PsiElement {
        return psiClass
    }

    override fun getPresentation(): ItemPresentation? {
        return psiClass.presentation
    }

}

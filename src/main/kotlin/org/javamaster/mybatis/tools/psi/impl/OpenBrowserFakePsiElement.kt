package org.javamaster.mybatis.tools.psi.impl

import com.intellij.ide.BrowserUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement

/**
 * @author yudong
 */
class OpenBrowserFakePsiElement(val url: String, val psiElement: PsiElement) : FakePsiElement() {

    override fun canNavigate(): Boolean {
        return true
    }

    override fun navigate(requestFocus: Boolean) {
        BrowserUtil.open(url)
    }

    override fun getName(): String {
        return "查看官网"
    }

    override fun getParent(): PsiElement {
        return psiElement
    }

}

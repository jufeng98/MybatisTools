package org.javamaster.mybatis.tools.reference

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.tip.hashmark.CompositeHashMarkTip
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.xml.DomUtil
import java.util.*

/**
 * @author yudong
 */
class MybatisFieldNameReference(
    psiElement: PsiElement,
    from: TextRange,
    private val nameLevels: LinkedList<String>,
) : PsiReferenceBase<PsiElement>(psiElement, from) {

    override fun resolve(): PsiElement? {
        val project = myElement.project

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(myElement) ?: return null

        val domElement = DomUtil.findDomElement(injectionHost, IdDomElement::class.java) ?: return null

        val psiMethod = domElement.id.value as? PsiMethod ?: return null

        return CompositeHashMarkTip.findReference(psiMethod, LinkedList<String>(nameLevels), project)
    }

}
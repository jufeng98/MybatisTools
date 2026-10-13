package org.javamaster.mybatis.tools.reference

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.tip.hashmark.CompositeHashMarkTip
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.xml.XmlElement
import com.intellij.util.xml.DomUtil
import org.apache.commons.collections.CollectionUtils
import java.util.*

/**
 * @author yudong
 */
class MybatisFieldNameReference(
    psiElement: PsiElement,
    val from: TextRange,
    private val nameLevels: LinkedList<String>,
) : PsiReferenceBase<PsiElement>(psiElement, from) {

    override fun resolve(): PsiElement? {
        val project = myElement.project

        val injectionHost = if (myElement is XmlElement) {
            myElement
        } else {
            InjectedLanguageManager.getInstance(project).getInjectionHost(myElement)
        } ?: return null

        val domElement = DomUtil.findDomElement(injectionHost, IdDomElement::class.java) ?: return null

        val psiMethod = domElement.id.value as? PsiMethod ?: return null

        return CompositeHashMarkTip.findReference(
            this, psiMethod, injectionHost, myElement, LinkedList<String>(nameLevels), project
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MybatisFieldNameReference) {
            return false
        }

        if (myElement != other.myElement) {
            return false
        }

        if (!CollectionUtils.isEqualCollection(nameLevels, other.nameLevels)) {
            return false
        }

        return from.substring(myElement.text) == other.from.substring(other.myElement.text)
    }

    override fun hashCode(): Int {
        var result = myElement.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + nameLevels.hashCode()
        return result
    }
}
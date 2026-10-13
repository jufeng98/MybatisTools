package org.javamaster.mybatis.tools.reference

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.tip.hashmark.CompositeHashMarkTip
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.xml.DomUtil
import org.javamaster.mybatis.tools.psi.MybatisParamAttr
import org.javamaster.mybatis.tools.psi.MybatisParamAttrValue

/**
 * @author yudong
 */
class MybatisAttrValueReference(
    psiElement: PsiElement,
    from: TextRange,
) : PsiReferenceBase<PsiElement>(psiElement, from) {

    override fun resolve(): PsiElement? {
        val attrValue = myElement as MybatisParamAttrValue
        val attr = attrValue.parent as MybatisParamAttr
        val attrName = attr.attrName.name

        val hashMarkTip = CompositeHashMarkTip.hashMarkTips
            .firstOrNull {
                it.name == attrName
            } ?: return null

        val injectionHost = InjectedLanguageManager.getInstance(attr.project).getInjectionHost(attr)
        val domElement = DomUtil.findDomElement(injectionHost, IdDomElement::class.java) ?: return null
        val mapper = MapperUtils.getMapper(domElement)

        return hashMarkTip.resolveValue(attrValue, mapper)
    }

}
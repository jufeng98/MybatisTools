package org.javamaster.mybatis.tools.reference

import com.baomidou.plugin.idea.mybatisx.tip.hashmark.CompositeHashMarkTip
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.mybatis.tools.handler.EqualsEndInsertHandler
import org.javamaster.mybatis.tools.psi.impl.OpenBrowserFakePsiElement

/**
 * @author yudong
 */
class MybatisAttrNameReference(
    psiElement: PsiElement,
    from: TextRange,
) : PsiReferenceBase<PsiElement>(psiElement, from) {

    override fun resolve(): PsiElement? {
        return OpenBrowserFakePsiElement("https://mybatis.org/mybatis-3/sqlmap-xml.html", myElement)
    }

    override fun getVariants(): Array<Any> {
        return CompositeHashMarkTip.hashMarkTips
            .map {
                LookupElementBuilder.create(it.name).withInsertHandler(EqualsEndInsertHandler)
            }
            .toTypedArray()
    }

}
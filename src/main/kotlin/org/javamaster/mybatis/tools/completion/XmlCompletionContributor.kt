package org.javamaster.mybatis.tools.completion

import com.baomidou.plugin.idea.mybatisx.tip.hashmark.CompositeHashMarkTip
import com.baomidou.plugin.idea.mybatisx.tip.hashmark.JavaTypeHashMark
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue

class XmlCompletionContributor : CompletionContributor() {
    private val set = setOf("resultType", "parameterType")

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        val psiElement = position.parent ?: return

        if (psiElement is XmlAttributeValue && MapperUtils.isElementWithinMybatisFile(psiElement)) {
            val attribute = psiElement.parent as XmlAttribute
            if (set.contains(attribute.name)) {
                val hashMarkTip = CompositeHashMarkTip.hashMarkTips
                    .first {
                        it is JavaTypeHashMark
                    }

                hashMarkTip.tipValue(parameters, result, null)
            }
        }
    }


}
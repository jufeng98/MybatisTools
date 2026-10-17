package org.javamaster.mybatis.tools.completion

import com.baomidou.plugin.idea.mybatisx.tip.hashmark.CompositeHashMarkTip
import com.baomidou.plugin.idea.mybatisx.tip.hashmark.JavaTypeHashMark
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue

/**
 * 取代了 AliasClassReference 的补全功能, 即其 getVariants 方法会被忽略
 *
 * @see com.baomidou.plugin.idea.mybatisx.alias.AliasClassReference
 *
 * @author yudong
 */
class XmlCompletionContributor : CompletionContributor() {
    private val set = setOf("resultType", "parameterType", "javaType", "ofType", "type")

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        val psiElement = position.parent ?: return

        if (psiElement is XmlAttributeValue && MapperUtils.isElementWithinMybatisFile(psiElement)) {
            val attribute = psiElement.parent as XmlAttribute
            val name = attribute.name

            if (set.contains(name)) {
                val hashMarkTip = CompositeHashMarkTip.hashMarkTips
                    .first {
                        it is JavaTypeHashMark
                    }

                hashMarkTip.tipValue(parameters, result, null)
            }
        }
    }


}
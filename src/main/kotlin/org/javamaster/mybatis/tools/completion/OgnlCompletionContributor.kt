package org.javamaster.mybatis.tools.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.ognl.psi.OgnlReferenceExpression
import org.javamaster.mybatis.tools.utils.CompletionUtils

/**
 * @author yudong
 */
class OgnlCompletionContributor : CompletionContributor() {

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        val psiElement = position.parent ?: return

        if (psiElement is OgnlReferenceExpression) {
            CompletionUtils.fillMybatisVariants(psiElement, parameters, result)
        }
    }

}

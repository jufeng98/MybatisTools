package org.javamaster.mybatis.tools.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.sql.psi.SqlIdentifier
import com.intellij.sql.psi.SqlReferenceExpression
import org.javamaster.mybatis.tools.utils.StringUtil

/**
 * @author yudong
 */
class SqlCompletionContributor : CompletionContributor() {

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        val parent = position.parent
        if (parent is SqlIdentifier) {
            tryFillColumnAliasGenerated(result, parent)
        }
    }

    private fun tryFillColumnAliasGenerated(result: CompletionResultSet, sqlIdentifier: SqlIdentifier) {
        val sqlReferenceExpression =
            PsiTreeUtil.getPrevSiblingOfType(sqlIdentifier, SqlReferenceExpression::class.java) ?: return

        val aliasName = StringUtil.toCamelCase(sqlReferenceExpression.name)
        val builder = LookupElementBuilder
            .create(aliasName)
            .bold()

        result.addElement(builder)
    }
}

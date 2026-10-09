package org.javamaster.mybatis.tools.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import org.javamaster.mybatis.tools.psi.MybatisFieldName
import org.javamaster.mybatis.tools.utils.CompletionUtils

class MybatisFieldNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        completionParameters: CompletionParameters,
        processingContext: ProcessingContext,
        completionResultSet: CompletionResultSet,
    ) {
        val position = completionParameters.position
        val psiElement = position.parent ?: return

        val mybatisFieldName = psiElement as MybatisFieldName

        CompletionUtils.fillMybatisVariants(mybatisFieldName, completionResultSet, false)
    }

}
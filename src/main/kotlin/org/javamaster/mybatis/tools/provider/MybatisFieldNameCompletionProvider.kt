package org.javamaster.mybatis.tools.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import org.javamaster.mybatis.tools.psi.MybatisParamFieldName
import org.javamaster.mybatis.tools.utils.CompletionUtils

class MybatisFieldNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        completionParameters: CompletionParameters,
        processingContext: ProcessingContext,
        completionResultSet: CompletionResultSet,
    ) {
        val position = completionParameters.position
        val psiElement = position.parent ?: return

        val mybatisFieldName = psiElement as MybatisParamFieldName

        CompletionUtils.fillMybatisVariants(mybatisFieldName, completionParameters, completionResultSet, false)
    }

}
package org.javamaster.mybatis.tools.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import org.javamaster.mybatis.tools.provider.MybatisAttrValueCompletionProvider
import org.javamaster.mybatis.tools.provider.MybatisFieldNameCompletionProvider
import org.javamaster.mybatis.tools.psi.MybatisTypes

/**
 * @author yudong
 */
class MybatisCompletionContributor : CompletionContributor() {

    init {
        this.extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(MybatisTypes.ATTR_VALUE_LABEL),
            MybatisAttrValueCompletionProvider()
        )

        this.extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(MybatisTypes.FIELD_NAME_LABEL),
            MybatisFieldNameCompletionProvider()
        )
    }

}

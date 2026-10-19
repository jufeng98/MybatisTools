package org.javamaster.mybatis.tools.utils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * @author yudong
 */
object InjectionUtils {

    fun innerRange(context: PsiElement): TextRange {
        val textRange = context.textRange
        return textRange.shiftLeft(textRange.startOffset)
    }

}
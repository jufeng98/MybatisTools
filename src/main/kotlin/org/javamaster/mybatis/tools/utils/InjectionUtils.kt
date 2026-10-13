package org.javamaster.mybatis.tools.utils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

object InjectionUtils {

    fun innerRange(context: PsiElement): TextRange {
        val textRange = context.textRange
        return textRange.shiftLeft(textRange.startOffset)
    }

}
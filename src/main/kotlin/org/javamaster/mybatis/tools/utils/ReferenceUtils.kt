package org.javamaster.mybatis.tools.utils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import org.javamaster.mybatis.tools.reference.MybatisFieldNameReference
import java.util.LinkedList

object ReferenceUtils {

    fun convert(text: String, psiElement: PsiElement): Array<PsiReference> {
        val textRangeList = convert(text)

        val psiReferences = mutableListOf<PsiReference>()

        for (i in textRangeList.indices) {
            val textRange = textRangeList[i]

            val nameLevels = LinkedList<String>()
            for (j in 0..<i + 1) {
                nameLevels.add(textRangeList[j].substring(text))
            }

            psiReferences.add(MybatisFieldNameReference(psiElement, textRange, nameLevels))
        }

        return psiReferences.toTypedArray()
    }

    fun convert(text: String): MutableList<TextRange> {
        val textRangeList: MutableList<TextRange> = mutableListOf()

        var lastIdx = 0
        var idx = text.indexOf(".")
        while (idx != -1) {
            val textRange = TextRange(lastIdx, idx)
            textRangeList.add(textRange)

            lastIdx = idx + 1
            idx = text.indexOf(".", lastIdx)
        }

        val textRange = TextRange(lastIdx, text.length)
        textRangeList.add(textRange)

        return textRangeList
    }

}

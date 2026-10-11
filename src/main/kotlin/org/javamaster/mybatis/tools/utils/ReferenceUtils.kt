package org.javamaster.mybatis.tools.utils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import org.javamaster.mybatis.tools.reference.MybatisFieldNameReference
import org.javamaster.mybatis.tools.reference.MybatisStaticClassMethodReference
import org.javamaster.mybatis.tools.reference.MybatisStaticClassReference
import java.util.*

object ReferenceUtils {

    fun convert(text: String, psiElement: PsiElement): Array<PsiReference> {
        val starts = text.startsWith("@")

        if (starts) {
            val psiReferences = convertStaticMethod(text, psiElement)

            return psiReferences.toTypedArray()
        } else {
            val psiReferences = mutableListOf<PsiReference>()

            val textRangeList = convertFieldName(text)

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
    }

    fun convertFieldName(text: String): MutableList<TextRange> {
        val textRangeList = mutableListOf<TextRange>()

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

    fun convertStaticMethod(text: String, psiElement: PsiElement): MutableList<PsiReference> {
        val psiReferences = mutableListOf<PsiReference>()

        if (text == "@") {
            return psiReferences
        }

        val idx = text.indexOf("@", 1)
        if (idx == -1) {
            val from = TextRange(1, text.length)
            val fullClassName = from.substring(text)
            psiReferences.add(MybatisStaticClassReference(psiElement, from, fullClassName))
        } else {
            val from = TextRange(1, idx)
            val fullClassName = from.substring(text)
            psiReferences.add(MybatisStaticClassReference(psiElement, from, fullClassName))

            if (text.length > idx + 1) {
                val paramIdx = text.indexOf("(", idx + 1)
                val textRange = if (paramIdx == -1) {
                    TextRange(idx + 1, text.length)
                } else {
                    TextRange(idx + 1, paramIdx)
                }

                psiReferences.add(MybatisStaticClassMethodReference(psiElement, textRange, fullClassName))
            }
        }

        return psiReferences
    }

}

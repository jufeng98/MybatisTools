package org.javamaster.mybatis.tools.utils

import com.intellij.lang.ognl.psi.OgnlIndexedExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceSet
import com.intellij.psi.util.InheritanceUtil
import org.javamaster.mybatis.tools.psi.impl.MapValueFakePsiElement
import org.javamaster.mybatis.tools.reference.OgnlFieldNameReference
import org.javamaster.mybatis.tools.reference.OgnlIndexFieldNameReference
import org.javamaster.mybatis.tools.reference.OgnlStaticClassMethodReference
import org.javamaster.mybatis.tools.reference.OgnlStaticClassReference
import org.javamaster.mybatis.tools.utils.PsiUtils.resolvePsiType
import java.util.*

/**
 * @author yudong
 */
object ReferenceUtils {

    fun resolveTarget(
        currentPsiElement: PsiElement,
        paramPsiCls: PsiClass,
        nameLevels: LinkedList<String>,
        oneParamAndNoParamAnno: Boolean,
    ): PsiElement? {
        var psiField: PsiField? = null

        try {
            var fieldTypeCls = paramPsiCls
            var propertyName = nameLevels.pop()

            if (oneParamAndNoParamAnno) {
                if (InheritanceUtil.isInheritor(paramPsiCls, "java.util.Collection")) {

                }
            }

            while (true) {
                if (InheritanceUtil.isInheritor(fieldTypeCls, "java.util.Map")) {
                    psiField = fieldTypeCls.findFieldByName(propertyName, true)
                    if (psiField == null) {
                        val psiMethods = fieldTypeCls.findMethodsByName(propertyName, true)
                        if (psiMethods.isNotEmpty()) {
                            return psiMethods[0]
                        }

                        return MapValueFakePsiElement(currentPsiElement, fieldTypeCls)
                    }

                    return MapValueFakePsiElement(currentPsiElement, fieldTypeCls)
                }

                psiField = fieldTypeCls.findFieldByName(propertyName, true)
                if (psiField == null) {
                    val psiMethods = fieldTypeCls.findMethodsByName(propertyName, true)
                    if (psiMethods.isNotEmpty()) {
                        return psiMethods[0]
                    }

                    return null
                }

                if (psiField.type !is PsiClassType) {
                    return psiField
                }

                val psiType = psiField.type as PsiClassType

                fieldTypeCls = resolvePsiType(psiType) ?: return null

                propertyName = nameLevels.pop()
            }
        } catch (_: NoSuchElementException) {
        }

        return psiField
    }

    fun convert(text: String, psiElement: PsiElement): Array<PsiReference> {
        val isStaticMethod = text.startsWith("@")

        if (isStaticMethod) {
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

                psiReferences.add(OgnlFieldNameReference(psiElement, textRange, nameLevels))
            }

            return psiReferences.toTypedArray()
        }
    }

    fun createIndexReferences(
        indexedExpression: OgnlIndexedExpression,
        textRanges: List<TextRange>,
        text: String,
    ): Array<PsiReference> {
        val psiReferences = mutableListOf<PsiReference>()

        for (i in textRanges.indices) {
            val textRange = textRanges[i]

            val nameLevels = LinkedList<String>()
            for (j in 0..<i + 1) {
                nameLevels.add(textRanges[j].substring(text))
            }

            psiReferences.add(OgnlIndexFieldNameReference(indexedExpression, textRange, nameLevels))
        }

        return psiReferences.toTypedArray()
    }

    fun convertFieldName(fieldName: String): MutableList<TextRange> {
        val textRangeList = mutableListOf<TextRange>()

        var lastIdx = 0
        var idx = fieldName.indexOf(".")
        while (idx != -1) {
            val textRange = TextRange(lastIdx, idx)
            textRangeList.add(textRange)

            lastIdx = idx + 1
            idx = fieldName.indexOf(".", lastIdx)
        }

        val textRange = TextRange(lastIdx, fieldName.length)
        textRangeList.add(textRange)

        return textRangeList
    }

    fun convertStaticMethod(text: String, psiElement: PsiElement): MutableList<PsiReference> {
        val psiReferences = mutableListOf<PsiReference>()

        if (text == "@") {
            return psiReferences
        }

        val referenceProvider = JavaClassReferenceProvider()

        val idx = text.indexOf("@", 1)
        if (idx == -1) {
            val from = TextRange(1, text.length)
            val fullClassName = from.substring(text)

            val referenceSet = JavaClassReferenceSet(fullClassName, psiElement, 1, false, referenceProvider)
            val references = referenceSet.references.map {
                OgnlStaticClassReference(psiElement, it.rangeInElement, it)
            }

            psiReferences.addAll(references)
        } else {
            val from = TextRange(1, idx)
            val fullClassName = from.substring(text)

            val referenceSet = JavaClassReferenceSet(fullClassName, psiElement, 1, false, referenceProvider)
            val references = referenceSet.references.map {
                OgnlStaticClassReference(psiElement, it.rangeInElement, it)
            }

            psiReferences.addAll(references)

            if (text.length > idx + 1) {
                val paramIdx = text.indexOf("(", idx + 1)
                val textRange = if (paramIdx == -1) {
                    TextRange(idx + 1, text.length)
                } else {
                    TextRange(idx + 1, paramIdx)
                }

                psiReferences.add(OgnlStaticClassMethodReference(psiElement, textRange, fullClassName))
            }
        }

        return psiReferences
    }

}

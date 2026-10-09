package org.javamaster.mybatis.tools.utils

import com.baomidou.plugin.idea.mybatisx.annotation.Annotation
import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.util.MethodParenthesesHandler
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.*
import com.intellij.psi.util.InheritanceUtil
import com.intellij.util.xml.DomUtil
import org.javamaster.mybatis.tools.reference.MybatisFieldNameReference
import java.util.*

/**
 * @author yudong
 */
object CompletionUtils {

    fun fillMybatisVariants(
        psiElement: PsiElement,
        completionResultSet: CompletionResultSet,
        fromOgnl: Boolean,
    ) {
        val xmlElement = InjectedLanguageManager.getInstance(psiElement.project).getInjectionHost(psiElement)

        val domElement = DomUtil.findDomElement(xmlElement, IdDomElement::class.java) ?: return

        val psiMethod = domElement.id.value as? PsiMethod ?: return

        fillVariants(psiMethod, psiElement, completionResultSet, fromOgnl)
    }

    private fun fillVariants(
        psiMethod: PsiMethod,
        psiElement: PsiElement,
        completionResultSet: CompletionResultSet,
        fromOgnl: Boolean,
    ) {
        val parameterList = psiMethod.parameterList
        if (parameterList.isEmpty) {
            return
        }

        val prefixMatcher = completionResultSet.prefixMatcher
        val text = psiElement.text

        val idx = text.indexOf(".")
        if (idx == -1) {
            val helper = JavaPsiFacade.getInstance(psiMethod.project).constantEvaluationHelper

            val parameters = parameterList.parameters

            for (psiParameter in parameters) {
                val annotation = psiParameter.getAnnotation(Annotation.PARAM.qualifiedName)
                if (annotation != null) {
                    val paramAnnotationValue = annotation.findAttributeValue("value")
                    val paramValue = helper.computeConstantExpression(paramAnnotationValue) as String? ?: continue
                    if (paramValue.isEmpty()) {
                        continue
                    }

                    fillParamNames(paramValue, psiParameter, completionResultSet, fromOgnl)
                } else {
                    val psiClass = PsiUtils.resolvePsiType(psiParameter.type) ?: return

                    fillFieldAndMethodNames("", psiClass, completionResultSet, fromOgnl)
                }
            }
        } else {
            val textRangeList = ReferenceUtils.convert(text)

            val psiReferences = mutableListOf<PsiReference>()

            for (i in textRangeList.indices) {
                val textRange = textRangeList[i]

                val nameLevels = LinkedList<String>()
                for (j in 0..<i + 1) {
                    nameLevels.add(textRangeList[j].substring(text))
                }

                psiReferences.add(MybatisFieldNameReference(psiElement, textRange, nameLevels))
            }

            val resolve = if (psiReferences.size == 1) {
                psiReferences[0].resolve()
            } else {
                psiReferences[psiReferences.size - 2].resolve()
            }

            val fieldPrefix = text.substring(0, idx + 1)

            val newPrefix = prefixMatcher.prefix
            // 按照新的前缀进行匹配提示
            val newCompletionResultSet = completionResultSet.withPrefixMatcher(newPrefix)
            if (resolve is PsiParameter || resolve is PsiField) {
                val psiClass = PsiUtils.resolvePsiType(resolve.type) ?: return

                fillFieldAndMethodNames(fieldPrefix, psiClass, newCompletionResultSet, fromOgnl)
            }
        }
    }

    private fun fillParamNames(
        paramValue: String,
        psiParameter: PsiParameter,
        completionResultSet: CompletionResultSet,
        fromOgnl: Boolean,
    ) {
        completionResultSet.addElement(
            LookupElementBuilder.create(psiParameter, paramValue).withTypeText(psiParameter.type.presentableText)
        )

        if (fromOgnl) {
            val psiClass = PsiUtils.resolvePsiType(psiParameter.type) ?: return
            var qualifiedName = psiClass.qualifiedName

            if (qualifiedName == "java.lang.String") {
                completionResultSet.addElement(
                    LookupElementBuilder.create(psiParameter, "$paramValue != null and $paramValue != ''")
                )
            } else if (InheritanceUtil.isInheritor(psiClass, "java.util.Collection")) {
                completionResultSet.addElement(
                    LookupElementBuilder.create(psiParameter, "$paramValue != null and $paramValue.size() > 0")
                )
            } else {
                completionResultSet.addElement(LookupElementBuilder.create(psiParameter, "$paramValue != null"))
            }
        }
    }

    private fun fillFieldAndMethodNames(
        fieldPrefix: String,
        psiClass: PsiClass,
        resultSet: CompletionResultSet,
        fromOgnl: Boolean,
    ) {
        val psiFields = psiClass.allFields
        psiFields.forEach {
            var lookupString = it.name
            resultSet.addElement(LookupElementBuilder.create(it, lookupString).withTypeText(it.type.presentableText))

            if (fromOgnl) {
                val psiClazz = PsiUtils.resolvePsiType(it.type)
                val qualifiedName = psiClazz?.qualifiedName
                lookupString = it.name

                if (qualifiedName == "java.lang.String") {
                    resultSet.addElement(
                        LookupElementBuilder.create(it, "$lookupString != null and ${fieldPrefix + lookupString} != ''")
                    )
                } else if (InheritanceUtil.isInheritor(psiClazz, "java.util.Collection")) {
                    resultSet.addElement(
                        LookupElementBuilder.create(
                            it,
                            "$lookupString != null and ${fieldPrefix + lookupString}.size() > 0"
                        )
                    )
                } else {
                    resultSet.addElement(LookupElementBuilder.create(it, "$lookupString != null"))
                }
            }
        }

        if (fromOgnl) {
            if (psiClass.qualifiedName?.startsWith("java") == true) {
                val set = mutableSetOf<String>()

                val allMethods = psiClass.allMethods
                allMethods.forEach {
                    val lookupString = it.name + it.parameterList.text
                    if (set.contains(lookupString)) {
                        return@forEach
                    }

                    set.add(lookupString)

                    resultSet.addElement(
                        LookupElementBuilder.create(it, it.name)
                            .withTypeText(it.returnType?.presentableText)
                            .withPresentableText(lookupString)
                            .withInsertHandler(MethodParenthesesHandler.getInstance(!it.parameterList.isEmpty))
                    )
                }
            }
        }
    }

}

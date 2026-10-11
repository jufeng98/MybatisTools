package org.javamaster.mybatis.tools.utils

import com.baomidou.plugin.idea.mybatisx.annotation.Annotation
import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.tip.hashmark.CompositeHashMarkTip
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.codeInsight.completion.JavaClassNameCompletionContributor
import com.intellij.codeInsight.completion.util.MethodParenthesesHandler
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import com.intellij.util.xml.DomUtil

/**
 * @author yudong
 */
object CompletionUtils {

    fun fillMybatisVariants(
        psiElement: PsiElement,
        parameters: CompletionParameters,
        completionResultSet: CompletionResultSet,
        fromOgnl: Boolean,
    ) {
        val injectionHost =
            InjectedLanguageManager.getInstance(psiElement.project).getInjectionHost(psiElement) ?: return

        val domElement = DomUtil.findDomElement(injectionHost, IdDomElement::class.java) ?: return

        val psiMethod = domElement.id.value as? PsiMethod ?: return

        fillVariants(psiMethod, psiElement, injectionHost, parameters, completionResultSet, fromOgnl)
    }

    private fun fillVariants(
        psiMethod: PsiMethod,
        currentPsiElement: PsiElement,
        injectionHost: PsiLanguageInjectionHost,
        parameters: CompletionParameters,
        resultSet: CompletionResultSet,
        fromOgnl: Boolean,
    ) {
        val parameterList = psiMethod.parameterList
        if (parameterList.isEmpty) {
            return
        }

        val prefix = CompletionUtil.findReferenceOrAlphanumericPrefix(parameters)

        var currentPsiElementText = currentPsiElement.text
        val project = psiMethod.project

        if (currentPsiElementText.startsWith("@")) {
            // 静态方法智能提示
            val idx = prefix.indexOf("@")
            val validPrefix = if (idx == -1) {
                prefix
            } else {
                prefix.substring(idx + 1)
            }

            val firstIdx = currentPsiElementText.indexOf("@")
            val lastIdx = currentPsiElementText.lastIndexOf("@")
            if (firstIdx == lastIdx) {
                val newCompletionResultSet = resultSet.withPrefixMatcher(validPrefix)

                JavaClassNameCompletionContributor.addAllClasses(
                    parameters, parameters.invocationCount <= 1,
                    newCompletionResultSet.prefixMatcher, newCompletionResultSet
                )
            } else {
                val fullClsName = currentPsiElementText.substring(firstIdx + 1, lastIdx)

                val psiClass = JavaPsiFacade.getInstance(project)
                    .findClass(fullClsName, GlobalSearchScope.everythingScope(project))
                if (psiClass == null) {
                    return
                }

                val newCompletionResultSet = resultSet.withPrefixMatcher(validPrefix)

                fillMethodNames(psiClass, newCompletionResultSet)
            }

            return
        }

        // 按照新的前缀进行匹配提示
        val newCompletionResultSet = resultSet.withPrefixMatcher(prefix)

        val collectionAttrValue = CompositeHashMarkTip.getCollectionAttrIfForeachTagItemAttrValue(injectionHost)
        if (collectionAttrValue != null) {
            // foreach 的 item 属性智能提示
            val value = collectionAttrValue.value

            val tip = if (value.endsWith("es")) {
                value.substring(0, value.length - 2)
            } else if (value.endsWith("List")) {
                value.substring(0, value.length - 4)
            } else {
                val psiClass = CompositeHashMarkTip.resolveGenericIfForeachTagItemAttrValue(collectionAttrValue)
                if (psiClass != null && psiClass.name != null) {
                    StringUtil.toCamelCase(psiClass.name)
                } else {
                    value.substring(0, value.length - 1)
                }
            }

            newCompletionResultSet.addElement(LookupElementBuilder.create(tip))

            return
        }

        val idx = currentPsiElementText.indexOf(".")

        val itemAttrValue = CompositeHashMarkTip.getItemAttrValueIfParamInForeachTag(injectionHost)
        if (itemAttrValue != null) {
            // foreach 里面的 param 参数属性智能提示
            if (idx == -1) {
                currentPsiElementText = itemAttrValue.value

                val psiReferences = ReferenceUtils.convert(currentPsiElementText, itemAttrValue)

                val itemPsiElement = psiReferences[0].resolve()
                if (itemPsiElement !is PsiClass) {
                    return
                }

                @Suppress("USELESS_CAST")
                val itemPsiClass = itemPsiElement as PsiClass

                newCompletionResultSet.addElement(
                    LookupElementBuilder.create(itemPsiClass, itemAttrValue.value).withTypeText(itemPsiClass.name)
                )
            } else {
                val psiReferences = ReferenceUtils.convert(currentPsiElementText, itemAttrValue)

                val itemPsiElement = psiReferences[0].resolve()
                if (itemPsiElement !is PsiClass) {
                    return
                }

                @Suppress("USELESS_CAST")
                val itemPsiClass = itemPsiElement as PsiClass

                fillFieldAndMethodNames("", itemPsiClass, newCompletionResultSet, fromOgnl)
            }

            return
        }

        if (idx == -1) {
            val helper = JavaPsiFacade.getInstance(project).constantEvaluationHelper

            val parameters = parameterList.parameters

            for (psiParameter in parameters) {
                val annotation = psiParameter.getAnnotation(Annotation.PARAM.qualifiedName)
                if (annotation != null) {
                    val paramAnnotationValue = annotation.findAttributeValue("value")
                    val paramValue = helper.computeConstantExpression(paramAnnotationValue) as String? ?: continue
                    if (paramValue.isEmpty()) {
                        continue
                    }

                    fillParamNames(paramValue, psiParameter, newCompletionResultSet, fromOgnl)
                } else {
                    val psiClass = PsiUtils.resolvePsiType(psiParameter.type) ?: return

                    fillFieldAndMethodNames("", psiClass, newCompletionResultSet, fromOgnl)
                }
            }
        } else {
            val psiReferences = ReferenceUtils.convert(currentPsiElementText, currentPsiElement)

            val resolve = if (psiReferences.size == 1) {
                psiReferences[0].resolve()
            } else {
                psiReferences[psiReferences.size - 2].resolve()
            }

            val fieldPrefix = currentPsiElementText.substring(0, idx + 1)

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
                fillMethodNames(psiClass, resultSet)
            }
        }
    }

    private fun fillMethodNames(psiClass: PsiClass, resultSet: CompletionResultSet) {
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

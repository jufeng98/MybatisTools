package org.javamaster.mybatis.tools.utils

import com.baomidou.plugin.idea.mybatisx.annotation.Annotation
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.ognl.psi.OgnlMethodCallExpression
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import org.javamaster.mybatis.tools.psi.impl.MapValueFakePsiElement
import org.javamaster.mybatis.tools.reference.OgnlFieldNameReference
import org.javamaster.mybatis.tools.utils.PsiUtils.constructParamMap
import org.javamaster.mybatis.tools.utils.PsiUtils.resolvePsiType
import org.javamaster.mybatis.tools.utils.XmlSqlUtils.getForeachTag
import java.util.*

/**
 * @author yudong
 */
object OgnlReferenceUtils {

    fun resolveTarget(
        ognlFieldNameReference: OgnlFieldNameReference, psiMethod: PsiMethod,
        injectionHost: PsiElement, currentPsiElement: PsiElement,
        nameLevels: LinkedList<String>, project: Project,
    ): PsiElement? {
        var psiElement = resolveGenericIfForeachTagItemAttrValue(injectionHost)
        if (psiElement != null) {
            return psiElement
        }

        psiElement = resolveGenericIfForeachTagIndexAttrValue(injectionHost)
        if (psiElement != null) {
            return psiElement
        }

        psiElement = resolveIfParamInForeachTag(ognlFieldNameReference, injectionHost, currentPsiElement)
        if (psiElement != null) {
            return psiElement
        }

        val parameterList = psiMethod.parameterList
        if (parameterList.isEmpty) {
            return null
        }

        val psiParameter = getPsiParameter(parameterList, nameLevels, project)
        if (psiParameter == null) {
            return null
        }

        if (nameLevels.isEmpty()) {
            return psiParameter
        }

        val oneParamAndNoParamAnno = PsiUtils.oneParamAndNoParamAnno(psiMethod)
        if (oneParamAndNoParamAnno) {
            val psiType = psiParameter.type
            if (psiType is PsiArrayType || InheritanceUtil.isInheritor(psiType, "java.util.Collection")) {
                return psiParameter
            }
        }

        val paramPsiType = psiParameter.type
        val paramPsiCls = resolvePsiType(paramPsiType)
        if (paramPsiCls == null) {
            return null
        }

        val paramMap = constructParamMap(parameterList.parameters)

        val resolveTarget = ReferenceUtils.resolveTarget(currentPsiElement, paramPsiCls, nameLevels, paramMap)
        if (resolveTarget != null) {
            return resolveTarget
        }

        if (oneParamAndNoParamAnno) {
            return psiParameter
        }

        return resolveTarget
    }

    fun getCollectionAttrIfForeachTagItemAttrValue(injectionHost: PsiElement?): XmlAttributeValue? {
        if (injectionHost !is XmlAttributeValue) {
            return null
        }

        val xmlAttribute = injectionHost.parent as XmlAttribute
        if ("item" != xmlAttribute.name) {
            return null
        }

        val xmlTag = xmlAttribute.parent
        val collectionAttr = xmlTag.getAttribute("collection")
        if (collectionAttr == null) {
            return null
        }

        return collectionAttr.valueElement
    }

    fun getCollectionAttrIfForeachTagIndexAttrValue(injectionHost: PsiElement?): XmlAttributeValue? {
        if (injectionHost !is XmlAttributeValue) {
            return null
        }

        val xmlAttribute = injectionHost.parent as XmlAttribute
        if ("index" != xmlAttribute.name) {
            return null
        }

        val xmlTag = xmlAttribute.parent
        val collectionAttr = xmlTag.getAttribute("collection")
        if (collectionAttr == null) {
            return null
        }

        return collectionAttr.valueElement
    }

    fun resolveGenericIfForeachTagItemAttrValue(collectionAttrValue: XmlAttributeValue): PsiClass? {
        val containingFile = collectionAttrValue.containingFile
        val textRange = collectionAttrValue.textRange

        val injectedLanguageManager = InjectedLanguageManager.getInstance(collectionAttrValue.project)
        val ognlPsiElement = injectedLanguageManager.findInjectedElementAt(containingFile, textRange.startOffset + 1)
        if (ognlPsiElement == null) {
            return null
        }

        val parent = ognlPsiElement.parent

        val psiReferences = parent.references

        val resolve = if (psiReferences.size == 1) {
            psiReferences[0].resolve()
        } else {
            psiReferences[psiReferences.size - 1].resolve()
        }

        val psiType = if (resolve is PsiParameter) {
            resolve.type
        } else if (resolve is PsiField) {
            resolve.type
        } else if (resolve is PsiMethod) {
            resolve.returnType
        } else if (resolve is MapValueFakePsiElement) {
            return resolve.psiClass
        } else {
            return null
        }

        val paramPsiCls = resolvePsiType(psiType)

        val paramIsCollection = InheritanceUtil.isInheritor(paramPsiCls, "java.util.Collection")
        if (paramIsCollection) {
            val genericParameters = (psiType as PsiClassReferenceType).parameters
            if (genericParameters.size == 0) {
                return null
            }

            return resolvePsiType(genericParameters[0])
        }

        val paramIsMap = InheritanceUtil.isInheritor(paramPsiCls, "java.util.Map")
        if (paramIsMap) {
            val genericParameters = (psiType as PsiClassReferenceType).parameters
            if (genericParameters.size < 2) {
                return null
            }

            val valueGenericParameter = genericParameters[1]
            return resolvePsiType(valueGenericParameter)
        }

        return paramPsiCls
    }

    fun resolveGenericIfForeachTagItemAttrValue(injectionHost: PsiElement?): PsiElement? {
        val collectionAttrValue = getCollectionAttrIfForeachTagItemAttrValue(injectionHost)
        if (collectionAttrValue == null) {
            return null
        }

        return resolveGenericIfForeachTagItemAttrValue(collectionAttrValue)
    }

    fun resolveGenericIfForeachTagIndexAttrValue(injectionHost: PsiElement?): PsiElement? {
        val collectionAttrValue = getCollectionAttrIfForeachTagIndexAttrValue(injectionHost)
        if (collectionAttrValue == null) {
            return null
        }

        return resolveGenericIfForeachTagIndexAttrValue(collectionAttrValue)
    }

    fun resolveGenericIfForeachTagIndexAttrValue(collectionAttrValue: XmlAttributeValue): PsiElement? {
        val collectionValue = collectionAttrValue.value

        val psiReferences = ReferenceUtils.convert(collectionValue, collectionAttrValue)

        val resolve = if (psiReferences.size == 1) {
            psiReferences[0].resolve()
        } else {
            psiReferences[psiReferences.size - 1].resolve()
        }

        val psiType = if (resolve is PsiParameter) {
            resolve.type
        } else if (resolve is PsiField) {
            resolve.type
        } else {
            return null
        }

        val paramPsiCls = resolvePsiType(psiType)

        val paramIsCollection = InheritanceUtil.isInheritor(paramPsiCls, "java.util.Collection")
        if (paramIsCollection) {
            val project = collectionAttrValue.project

            return JavaPsiFacade.getInstance(project)
                .findClass("java.lang.Integer", GlobalSearchScope.allScope(project))
        }

        val paramIsMap = InheritanceUtil.isInheritor(paramPsiCls, "java.util.Map")
        if (paramIsMap) {
            val genericParameters = (psiType as PsiClassReferenceType).parameters
            if (genericParameters.size < 1) {
                return null
            }

            val keyGenericParameter = genericParameters[0]
            return resolvePsiType(keyGenericParameter)
        }

        return paramPsiCls
    }

    fun getItemAttrValueIfParamInForeachTag(injectionHost: PsiElement): XmlAttributeValue? {
        val parent = injectionHost.parent
        if (parent !is XmlTag) {
            return null
        }

        val tagName = parent.name
        if ("foreach" != tagName) {
            return null
        }

        val itemAttr = parent.getAttribute("item")
        if (itemAttr == null) {
            return null
        }

        return itemAttr.valueElement
    }

    fun resolveIfParamInForeachTag(
        ognlFieldNameReference: OgnlFieldNameReference,
        injectionHost: PsiElement,
        currentPsiElement: PsiElement,
    ): PsiElement? {
        val foreachTag = getForeachTag(injectionHost)
        if (foreachTag == null) {
            return null
        }

        val references = currentPsiElement.references
        if (references.isEmpty()) {
            return null
        }

        val firstPsiReference = references[0]
        if (firstPsiReference !is OgnlFieldNameReference) {
            return null
        }

        val collectionAttr = foreachTag.getAttribute("collection")
        if (collectionAttr == null) {
            return null
        }

        val collectionAttrValue = collectionAttr.valueElement
        if (collectionAttrValue == null) {
            return null
        }

        val itemAttr = foreachTag.getAttribute("item")

        val indexAttr = foreachTag.getAttribute("index")

        val text = currentPsiElement.text

        val firstStr = firstPsiReference.from.substring(text)

        var collectionPsiElement = if (firstStr == itemAttr?.value) {
            resolveGenericIfForeachTagItemAttrValue(collectionAttrValue)
        } else if (firstStr == indexAttr?.value) {
            resolveGenericIfForeachTagIndexAttrValue(collectionAttrValue)
        } else {
            null
        } ?: return null

        if (firstPsiReference == ognlFieldNameReference) {
            return collectionPsiElement
        }

        if (collectionPsiElement == null) {
            return null
        }

        if (references.size < 2) return null

        if (collectionPsiElement !is PsiClass) {
            return null
        }

        return resolveJavaElement(references, ognlFieldNameReference, collectionPsiElement, text)
    }

    fun getPsiParameter(
        parameterList: PsiParameterList,
        nameLevel: LinkedList<String>,
        project: Project,
    ): PsiParameter? {
        if (nameLevel.isEmpty()) {
            return null
        }

        val helper = JavaPsiFacade.getInstance(project).constantEvaluationHelper

        val name = nameLevel.pop()
        val parametersCount = parameterList.parametersCount
        val parameters = parameterList.parameters

        for (psiParameter in parameters) {
            val annotation = psiParameter.getAnnotation(Annotation.PARAM.qualifiedName)
            if (annotation != null) {
                val paramAnnotationValue = annotation.findAttributeValue("value")
                val paramValue = helper.computeConstantExpression(paramAnnotationValue) as String?
                // 设置了 @Param 但是内容是空, 这种情况不予处理
                if (paramValue == null || paramValue.isBlank()) {
                    continue
                }

                if (paramValue == name) {
                    return psiParameter
                }
            } else {
                // @Param 注解一定为空的情况下的跳转处理
                // 兼容java8开启 -parameters 参数的方式，这种方式不需要 @Param 参数
                if (psiParameter.name == name) {
                    return psiParameter
                }

                if (parametersCount == 1) {
                    nameLevel.push(name)
                    return psiParameter
                }
            }
        }

        return null
    }

    private fun resolveJavaElement(
        references: Array<PsiReference>,
        ognlFieldNameReference: OgnlFieldNameReference,
        collectionPsiElement: PsiClass,
        text: String,
    ): PsiElement? {
        var currentPsiClass = collectionPsiElement

        for (index in 1 until references.size) {
            val psiReference = references[index]
            if (psiReference !is OgnlFieldNameReference) {
                return null
            }

            val fieldName = psiReference.from.substring(text)

            // 命中目标引用：用当前所在类解析字段/方法
            if (psiReference == ognlFieldNameReference) {
                return findJavaElement(fieldName, currentPsiClass, psiReference)
            }

            val psiElement = findJavaElement(fieldName, currentPsiClass, psiReference)

            val resolvedType = when (psiElement) {
                is PsiField -> psiElement.type
                is PsiMethod -> psiElement.returnType ?: return null
                else -> return null
            }

            currentPsiClass = resolvePsiType(resolvedType) ?: return null
        }

        return null
    }

    private fun findJavaElement(
        fieldName: String,
        collectionPsiElement: PsiClass,
        psiReference: OgnlFieldNameReference,
    ): PsiElement? {
        val psiField = collectionPsiElement.findFieldByName(fieldName, true)
        if (psiField != null) {
            return psiField
        }

        val psiMethods = collectionPsiElement.findMethodsByName(fieldName, true)
        if (psiMethods.isEmpty()) {
            return null
        }

        val element = psiReference.element
        val methodCallExpression = element.parent as? OgnlMethodCallExpression
        val count = methodCallExpression?.parameterList?.parameterCount ?: 0

        // 简单判断方法参数数量
        for (method in psiMethods) {
            if (method.parameterList.parametersCount == count) {
                return method
            }
        }

        return psiMethods[0]
    }
}

package org.javamaster.mybatis.tools.utils

import com.baomidou.plugin.idea.mybatisx.dom.model.Bind
import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.util.MybatisConstants
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.ognl.psi.OgnlIndexedExpression
import com.intellij.lang.ognl.psi.OgnlMethodCallExpression
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomUtil
import org.javamaster.mybatis.tools.psi.impl.MapValueFakePsiElement
import org.javamaster.mybatis.tools.reference.OgnlFieldNameReference
import org.javamaster.mybatis.tools.utils.PsiUtils.constructParamMap
import org.javamaster.mybatis.tools.utils.PsiUtils.getIfOneParamAndNoParamAnno
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

        psiElement = resolveIfMatchBindName(ognlFieldNameReference, injectionHost, currentPsiElement)
        if (psiElement != null) {
            return psiElement
        }

        val parameterList = psiMethod.parameterList
        val parametersCount = parameterList.parametersCount
        if (parameterList.isEmpty) {
            return null
        }

        if (nameLevels.isEmpty()) {
            return null
        }

        val onlyParameterWithNoParamAnno = getIfOneParamAndNoParamAnno(psiMethod)
        if (onlyParameterWithNoParamAnno != null) {
            val psiType = onlyParameterWithNoParamAnno.type
            if (psiType is PsiArrayType
                || InheritanceUtil.isInheritor(psiType, MybatisConstants.COLLECTION)
                || InheritanceUtil.isInheritor(psiType, MybatisConstants.MAP)
            ) {
                return onlyParameterWithNoParamAnno
            }
        }

        val paramMap = constructParamMap(parameterList.parameters, project)

        val firstName = nameLevels.pop()
        if (onlyParameterWithNoParamAnno != null) {
            nameLevels.push(firstName)
        }

        val psiParameter = paramMap[firstName]
        val paramPsiCls = if (psiParameter != null) {
            val paramPsiType = psiParameter.type
            resolvePsiType(paramPsiType)
        } else {
            if (onlyParameterWithNoParamAnno != null) {
                val paramPsiType = onlyParameterWithNoParamAnno.type
                resolvePsiType(paramPsiType)
            } else if (firstName == "_parameter") {
                JavaPsiFacade.getInstance(project).findClass(MybatisConstants.MAP, GlobalSearchScope.allScope(project))
            } else if (parametersCount == 1) {
                val paramPsiType = parameterList.parameters[0].type
                resolvePsiType(paramPsiType)
            } else {
                null
            }
        }

        if (nameLevels.isEmpty()) {
            return psiParameter ?: paramPsiCls
        }

        if (paramPsiCls == null) {
            return onlyParameterWithNoParamAnno ?: psiParameter
        }

        val resolveTarget = ReferenceUtils.resolveTarget(currentPsiElement, paramPsiCls, nameLevels, paramMap)
        if (resolveTarget != null) {
            return resolveTarget
        }

        if (onlyParameterWithNoParamAnno != null) {
            return onlyParameterWithNoParamAnno
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
        val ognlPsiElement = injectedLanguageManager.findInjectedElementAt(
            containingFile,
            (textRange.startOffset + 1).coerceAtLeast(textRange.endOffset - 2)
        )
        if (ognlPsiElement == null) {
            return null
        }

        val parent = ognlPsiElement.parent

        val psiReferences = parent.references

        val resolve = psiReferences.lastOrNull()?.resolve()

        val pair = PsiUtils.resolveActualType(resolve, true)

        val psiClass = pair?.second ?: pair?.first ?: return null

        return psiClass
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
        val containingFile = collectionAttrValue.containingFile
        val textRange = collectionAttrValue.textRange

        val injectedLanguageManager = InjectedLanguageManager.getInstance(collectionAttrValue.project)
        val ognlPsiElement = injectedLanguageManager.findInjectedElementAt(
            containingFile,
            (textRange.startOffset + 1).coerceAtLeast(textRange.endOffset - 2)
        )
        if (ognlPsiElement == null) {
            return null
        }

        val parent = ognlPsiElement.parent

        val psiReferences = parent.references

        val resolve = psiReferences.lastOrNull()?.resolve()

        val pair = PsiUtils.resolveActualType(resolve) ?: return null

        val psiClass = pair.first
        val psiType = pair.second

        val paramIsCollection = InheritanceUtil.isInheritor(psiClass, MybatisConstants.COLLECTION)
        if (paramIsCollection) {
            val project = collectionAttrValue.project

            return JavaPsiFacade.getInstance(project)
                .findClass(MybatisConstants.INTEGER, GlobalSearchScope.allScope(project))
        }

        val paramIsMap = InheritanceUtil.isInheritor(psiClass, MybatisConstants.MAP)
        if (paramIsMap) {
            val genericParameters = (psiType as PsiClassReferenceType).parameters
            if (genericParameters.size < 2) {
                return null
            }

            val keyGenericParameter = genericParameters[0]
            return resolvePsiType(keyGenericParameter)
        }

        return psiClass
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
        val foreachTag = getForeachTag(injectionHost) ?: return null

        val references = currentPsiElement.references
        if (references.isEmpty()) {
            return null
        }

        val firstPsiReference = references[0]
        if (firstPsiReference !is OgnlFieldNameReference) {
            return null
        }

        val collectionAttr = foreachTag.getAttribute("collection") ?: return null

        val collectionAttrValue = collectionAttr.valueElement ?: return null

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

        if (references.size < 2) return null

        if (collectionPsiElement !is PsiClass) {
            return null
        }

        return resolveJavaElement(references, ognlFieldNameReference, collectionPsiElement, text)
    }

    private fun resolveIfMatchBindName(
        ognlFieldNameReference: OgnlFieldNameReference,
        injectionHost: PsiElement,
        currentPsiElement: PsiElement,
    ): PsiElement? {
        val domElement = DomUtil.findDomElement(injectionHost, IdDomElement::class.java) ?: return null

        val xmlTags = PsiTreeUtil.findChildrenOfType(domElement.xmlElement, XmlTag::class.java)
        if (xmlTags.isEmpty()) {
            return null
        }

        val bind = DomUtil.getDomElement(injectionHost.parent?.parent) as? Bind
        if (bind != null) {
            // 排除 bind 标签自身
            return null
        }

        val references = currentPsiElement.references
        if (references.isEmpty()) {
            return null
        }

        val nameLevels = LinkedList<String>(ognlFieldNameReference.nameLevels)
        val firstName = nameLevels.pop()

        val bindXmlTag = xmlTags.firstOrNull {
            it.name == "bind" && it.getAttributeValue("name") == firstName
        } ?: return null

        val bindDomElement = DomUtil.getDomElement(bindXmlTag) as? Bind ?: return null

        val bindValuePsiElement = bindDomElement.name.value ?: return null

        val firstReference = references[0]

        if (ognlFieldNameReference == firstReference) {
            return bindValuePsiElement
        }

        val pair = PsiUtils.resolveActualType(bindValuePsiElement, true)

        val psiClass = pair?.first ?: return bindValuePsiElement

        return ReferenceUtils.resolveTarget(currentPsiElement, psiClass, nameLevels, emptyMap())
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

            val pair = PsiUtils.resolveActualType(psiElement, true)

            currentPsiClass = pair?.first ?: return psiElement
        }

        return null
    }

    private fun findJavaElement(
        fieldName: String,
        currentPsiClass: PsiClass,
        psiReference: OgnlFieldNameReference,
    ): PsiElement? {
        val psiField = currentPsiClass.findFieldByName(fieldName, true)
        if (psiField != null) {
            return psiField
        }

        var psiMethods = ReferenceUtils.findPossibleMethods(currentPsiClass, fieldName)
        if (psiMethods.isNotEmpty()) {
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

        if (InheritanceUtil.isInheritor(currentPsiClass, MybatisConstants.MAP)) {
            return MapValueFakePsiElement(currentPsiClass, currentPsiClass)
        }

        return null
    }

    fun resolveBindValueAttr(valueAttributeValue: XmlAttributeValue): PsiElement? {
        val containingFile = valueAttributeValue.containingFile
        val textRange = valueAttributeValue.textRange
        val project = valueAttributeValue.project

        val offset = (textRange.startOffset + 1).coerceAtLeast(textRange.endOffset - 2)

        val injectedLanguageManager = InjectedLanguageManager.getInstance(project)

        val ognlPsiElement = injectedLanguageManager.findInjectedElementAt(containingFile, offset) ?: return null

        var isIndexAccess = false

        val parent = ognlPsiElement.parent
        var psiReferences = parent.references
        if (psiReferences.isEmpty()) {
            if (parent is OgnlIndexedExpression) {
                psiReferences = parent.referenceQualifier.references
                isIndexAccess = true
            }
        }

        val resolve = psiReferences.lastOrNull()?.resolve()

        if (isIndexAccess) {
            val pair = PsiUtils.resolveActualType(resolve, true)

            return pair?.second ?: pair?.first ?: resolve
        }

        return resolve
    }

}

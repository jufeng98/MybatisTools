package org.javamaster.mybatis.tools.utils

import com.baomidou.plugin.idea.mybatisx.annotation.Annotation
import com.baomidou.plugin.idea.mybatisx.util.MybatisConstants
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.xml.XmlText
import org.apache.commons.lang3.StringUtils
import org.javamaster.mybatis.tools.psi.impl.MapValueFakePsiElement

/**
 * @author yudong
 */
object PsiUtils {

    fun resolvePsiType(psiType: PsiType?): PsiClass? {
        var type = psiType
        if (type is PsiArrayType) {
            type = type.componentType
        }

        if (type is PsiClassReferenceType) {
            return type.resolve()
        }

        return null
    }

    fun resolvePsiClass(fullClassName: String, module: Module?, project: Project): PsiClass? {
        val scope = if (module != null) {
            GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
        } else {
            GlobalSearchScope.everythingScope(project)
        }

        val javaPsiFacade = JavaPsiFacade.getInstance(project)

        return javaPsiFacade.findClass(fullClassName, scope)
    }

    fun getIfOneParamAndNoParamAnno(psiMethod: PsiMethod?): PsiParameter? {
        psiMethod ?: return null

        val parameterList = psiMethod.parameterList
        val parametersCount = parameterList.parametersCount
        if (parametersCount == 0) {
            return null
        }

        if (parametersCount > 1) {
            return null
        }

        val parameters = parameterList.parameters
        val parameter = parameters[0]
        if (parameter.getAnnotation(Annotation.PARAM.qualifiedName) != null) {
            return null
        }

        return parameter
    }

    fun getNextNotBlankXmlText(xmlTexts: Collection<XmlText>, start: Int): XmlText? {
        var idx = start
        while (true) {
            var next = xmlTexts.elementAtOrNull(idx)

            if (next == null) {
                return null
            }

            val value = next.value
            if (StringUtils.isNotBlank(value)) {
                return next
            }

            idx++
        }
    }

    fun constructParamMap(parameters: Array<PsiParameter>?, project: Project): Map<String, PsiParameter> {
        val map = mutableMapOf<String, PsiParameter>()

        val helper = JavaPsiFacade.getInstance(project).constantEvaluationHelper

        var idx = 0
        parameters?.forEach {
            val psiType = it.type
            if (InheritanceUtil.isInheritor(psiType, "org.apache.ibatis.session.RowBounds")
                || InheritanceUtil.isInheritor(psiType, "org.apache.ibatis.session.ResultHandler")
            ) {
                return@forEach
            }

            val annotation = it.getAnnotation(Annotation.PARAM.qualifiedName)
            if (annotation != null) {
                val paramAnnotationValue = annotation.findAttributeValue("value")
                val paramValue = helper.computeConstantExpression(paramAnnotationValue) as String?
                if (StringUtils.isNotBlank(paramValue)) {
                    map[paramValue!!] = it
                }

            } else {
                map[it.name] = it
            }

            val index = idx++
            map["param$index"] = it
            map["arg${index}"] = it
        }

        return map
    }

    fun resolveActualType(psiElement: PsiElement?): Pair<PsiClass?, PsiType?>? {
        val psiType = if (psiElement is PsiParameter) {
            psiElement.type
        } else if (psiElement is PsiField) {
            psiElement.type
        } else if (psiElement is PsiMethod) {
            psiElement.returnType
        } else if (psiElement is PsiClass) {
            return Pair(psiElement, null)
        } else {
            return null
        }

        val psiClass = resolvePsiType(psiType)

        return Pair(psiClass, psiType)
    }

    /**
     * 解析 psiElement 的类型
     * @param valueType 只对 Map 类型起作用,决定取 key 还是 value 的泛型类型
     * @return first 为 psiElement 的类型, second 为其泛型(对于 Collection 和 Map)
     */
    fun resolveActualType(psiElement: PsiElement?, valueType: Boolean): Pair<PsiClass?, PsiClass?>? {
        if (psiElement is MapValueFakePsiElement) {
            val clazz = psiElement.psiClass
            val typeParameter = if (valueType) {
                clazz.typeParameters.lastOrNull()
            } else {
                clazz.typeParameters.firstOrNull()
            }

            return Pair(clazz, typeParameter)
        }

        val pair = resolveActualType(psiElement) ?: return null

        val psiClass = pair.first
        val psiType = pair.second

        val paramIsCollection = InheritanceUtil.isInheritor(psiClass, MybatisConstants.COLLECTION)
        if (paramIsCollection) {
            val genericParameters = (psiType as PsiClassReferenceType).parameters
            if (genericParameters.size == 0) {
                return null
            }

            return Pair(psiClass, resolvePsiType(genericParameters[0]))
        }

        val paramIsMap = InheritanceUtil.isInheritor(psiClass, MybatisConstants.MAP)
        if (paramIsMap) {
            val genericParameters = (psiType as PsiClassReferenceType).parameters
            if (genericParameters.size < 2) {
                return null
            }

            val idx = if (valueType) 1 else 0
            val genericParameter = genericParameters[idx]
            return Pair(psiClass, resolvePsiType(genericParameter))
        }

        return Pair(psiClass, null)
    }

}

package org.javamaster.mybatis.tools.utils

import com.baomidou.plugin.idea.mybatisx.annotation.Annotation
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.xml.XmlText
import org.apache.commons.lang3.StringUtils

/**
 * @author yudong
 */
object PsiUtils {

    fun resolvePsiType(psiType: PsiType?): PsiClass? {
        if (psiType !is PsiClassReferenceType) {
            return null
        }

        return psiType.resolve()
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

}

package org.javamaster.mybatis.tools.reference

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.dom.model.Sql
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.ognl.psi.OgnlIndexedExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.InheritanceUtil
import com.intellij.util.xml.DomUtil
import org.javamaster.mybatis.tools.utils.MapperUtil
import org.javamaster.mybatis.tools.utils.PsiUtils
import org.javamaster.mybatis.tools.utils.PsiUtils.constructParamMap
import org.javamaster.mybatis.tools.utils.ReferenceUtils.resolveTarget
import java.util.*

/**
 * @author yudong
 */
class OgnlIndexFieldNameReference(
    indexedExpression: OgnlIndexedExpression,
    val from: TextRange,
    private val nameLevels: LinkedList<String>,
) : PsiReferenceBase<OgnlIndexedExpression>(indexedExpression, from) {

    override fun resolve(): PsiElement? {
        val referenceQualifier = myElement.referenceQualifier
        val psiReferences = referenceQualifier.references
        if (psiReferences.isEmpty()) {
            return null
        }

        val resolve = psiReferences.last().resolve()

        val genericPsiClass = if (resolve is PsiParameter || resolve is PsiField) {
            val genericParameters = (resolve.type as PsiClassReferenceType).parameters
            if (genericParameters.isEmpty()) {
                null
            } else {
                PsiUtils.resolvePsiType(genericParameters[0]) ?: return null
            }
        } else {
            null
        } ?: return null

        val injectionHost =
            InjectedLanguageManager.getInstance(myElement.project).getInjectionHost(myElement) ?: return null

        val domElement = DomUtil.findDomElement(injectionHost, IdDomElement::class.java) ?: return null

        val psiMethod = if (domElement is Sql) {
            MapperUtil.getSqlPsiMethod(domElement)
        } else {
            domElement.id.value as? PsiMethod
        }

        val oneParamAndNoParamAnno = PsiUtils.oneParamAndNoParamAnno(psiMethod)
        if (oneParamAndNoParamAnno) {
            val psiParameter = psiMethod!!.parameterList.parameters[0]
            val psiType = psiParameter.type
            if (psiType is PsiArrayType || InheritanceUtil.isInheritor(psiType, "java.util.Collection")) {
                return psiParameter
            }
        }

        val paramMap = constructParamMap(psiMethod?.parameterList?.parameters)

        return resolveTarget(myElement, genericPsiClass, nameLevels, paramMap)
    }

}
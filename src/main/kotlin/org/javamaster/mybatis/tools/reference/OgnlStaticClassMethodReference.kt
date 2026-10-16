package org.javamaster.mybatis.tools.reference

import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.mybatis.tools.utils.PsiUtils

/**
 * @author yudong
 */
class OgnlStaticClassMethodReference(
    psiElement: PsiElement,
    val from: TextRange,
    private val fullClassName: String,
) : PsiReferenceBase<PsiElement>(psiElement, from) {

    override fun resolve(): PsiElement? {
        val module = ModuleUtil.findModuleForPsiElement(myElement)

        val psiClass = PsiUtils.resolvePsiClass(fullClassName, module, myElement.project) ?: return null

        val text = myElement.parent.text

        val methodName = from.substring(text)
        val methods = psiClass.findMethodsByName(methodName, true)
        if (methods.isEmpty()) {
            return null
        }

        val params = text.substring(from.endOffset + 1).split(",")

        // 简单判断方法参数数量
        for (method in methods) {
            if (method.parameterList.parametersCount != params.size) {
                return method
            }
        }

        return methods[0]
    }

}
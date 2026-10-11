package org.javamaster.mybatis.tools.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.base.codeInsight.handlers.fixers.end

/**
 * @author yudong
 */
class MybatisStaticClassMethodReference(
    psiElement: PsiElement,
    private val from: TextRange,
    private val fullClassName: String,
) : PsiReferenceBase<PsiElement>(psiElement, from) {

    override fun resolve(): PsiElement? {
        val project = myElement.project
        val psiClass =
            JavaPsiFacade.getInstance(project).findClass(fullClassName, GlobalSearchScope.everythingScope(project))
                ?: return null

        val text = myElement.parent.text

        val methodName = from.substring(text)
        val methods = psiClass.findMethodsByName(methodName, true)
        if (methods.isEmpty()) {
            return null
        }

        val params = text.substring(from.end + 1).split(",")

        // 简单判断方法参数数量
        for (method in methods) {
            if (method.parameterList.parametersCount != params.size) {
                return method
            }
        }

        return methods[0]
    }

}
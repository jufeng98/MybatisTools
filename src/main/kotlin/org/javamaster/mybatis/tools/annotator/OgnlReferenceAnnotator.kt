package org.javamaster.mybatis.tools.annotator

import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.ognl.psi.OgnlReferenceExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.javamaster.mybatis.tools.reference.OgnlFieldNameReference
import org.javamaster.mybatis.tools.reference.OgnlStaticClassMethodReference
import org.javamaster.mybatis.tools.reference.OgnlStaticClassReference

/**
 * @author yudong
 */
class OgnlReferenceAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val project = element.project

        if (element !is OgnlReferenceExpression) {
            return
        }

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(element)
        if (injectionHost !is PsiElement) {
            return
        }

        if (!MapperUtils.isElementWithinMybatisFile(injectionHost)) {
            return
        }

        val references = element.references
        if (references.isEmpty()) {
            return
        }

        for (reference in references) {
            if (reference is OgnlFieldNameReference) {
                val resolve = reference.resolve()
                if (resolve == null) {
                    annoOgnl(holder, reference.absoluteRange, "无法解析字段或方法")
                }
            } else if (reference is OgnlStaticClassReference) {
                val resolve = reference.resolve()
                if (resolve == null) {
                    annoOgnl(holder, reference.absoluteRange, "无法解析包名或类名")
                }
            } else if (reference is OgnlStaticClassMethodReference) {
                val resolve = reference.resolve()
                if (resolve == null) {
                    annoOgnl(holder, reference.absoluteRange, "无法解析方法")
                }
            }
        }
    }

    private fun annoOgnl(holder: AnnotationHolder, from: TextRange, tip: String) {
        holder.newAnnotation(HighlightSeverity.ERROR, tip)
            .range(from)
            .create()
    }

}

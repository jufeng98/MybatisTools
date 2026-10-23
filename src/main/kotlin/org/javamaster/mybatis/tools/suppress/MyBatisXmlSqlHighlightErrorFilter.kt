package org.javamaster.mybatis.tools.suppress

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.xml.XmlTag
import com.intellij.sql.psi.SqlLanguage

/**
 * @author yudong
 */
class MyBatisXmlSqlHighlightErrorFilter : HighlightErrorFilter() {

    /**
     * 抑制 sql 标签里的 sql 语法错误
     */
    override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
        val language = element.language
        val project = element.project

        if (language.baseLanguage !is SqlLanguage) {
            return true
        }

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(element)
        if (injectionHost == null) {
            return true
        }

        val parent = injectionHost.parent

        val inSqlTag = inSqlTag(parent)

        return !inSqlTag
    }

    private fun inSqlTag(parent: PsiElement?): Boolean {
        parent ?: return false

        if (parent is XmlTag) {
            if (parent.name == "sql") {
                return true
            }
        }

        return inSqlTag(parent.parent)
    }

}

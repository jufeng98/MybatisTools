package org.javamaster.mybatis.tools.utils

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.sql.psi.SqlFile
import com.intellij.sql.psi.SqlSelectClause

/**
 * @author yudong
 */
object XmlSqlUtils {

    fun collectColumnNames(selectXmlTag: XmlTag?, project: Project): Set<String>? {
        if (selectXmlTag == null) {
            return null
        }

        val xmlTexts = PsiTreeUtil.findChildrenOfType(selectXmlTag, XmlText::class.java)

        val injectedLanguageManager = InjectedLanguageManager.getInstance(project)

        var injectedPsiFiles: List<Pair<PsiElement?, TextRange?>?>? = null

        for (xmlText in xmlTexts) {
            injectedPsiFiles = injectedLanguageManager.getInjectedPsiFiles(xmlText)
            if (injectedPsiFiles == null) {
                continue
            }

            if (injectedPsiFiles.isNotEmpty() == true) {
                break
            }
        }

        val sqlFile = injectedPsiFiles?.get(0)?.first as? SqlFile ?: return null

        val firstSelectClause = PsiTreeUtil.findChildOfType(sqlFile, SqlSelectClause::class.java) ?: return null

        val set = firstSelectClause.expressions
            .mapNotNull { it.name }
            .toSet()

        if (set.isEmpty()) {
            return null
        }

        return set
    }

}

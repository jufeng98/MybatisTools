package org.javamaster.mybatis.tools.utils

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.sql.psi.SqlExpression
import com.intellij.sql.psi.SqlFile
import com.intellij.sql.psi.SqlSelectClause
import com.intellij.util.xml.DomUtil

/**
 * @author yudong
 */
object XmlSqlUtils {

    fun getRelateColumnElements(attribute: XmlAttribute): Set<SqlExpression> {
        val xmlTag = attribute.parent
        val parentXmlTag = xmlTag.parent
        if (parentXmlTag !is XmlTag) {
            return emptySet()
        }

        val resultList = mutableListOf<LookupElementBuilder>()

        val propertyValue = xmlTag.getAttributeValue("property")

        if (!propertyValue.isNullOrBlank()) {
            resultList.add(LookupElementBuilder.create(propertyValue))

            val underlineName = StringUtil.camelToUnderline(propertyValue)
            resultList.add(LookupElementBuilder.create(underlineName))
        }

        if (!DatabasePluginUtils.databasePluginAvailable) {
            return emptySet()
        }

        val domElement = DomUtil.findDomElement(attribute, IdDomElement::class.java) ?: return emptySet()

        val resultMapId = MapperUtils.getResultMapId(parentXmlTag) ?: return emptySet()

        val mapper = MapperUtils.getMapper(domElement)

        val firstSelect = mapper.selects
            .firstOrNull { it.xmlTag?.getAttributeValue("resultMap") == resultMapId }

        val firstSelectXmlTag = firstSelect?.xmlTag

        return collectColumnNameElements(firstSelectXmlTag, parentXmlTag.project) ?: return emptySet()
    }

    fun collectColumnNameElements(selectXmlTag: XmlTag?, project: Project): Set<SqlExpression>? {
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
            .mapNotNull { it }
            .toSet()

        if (set.isEmpty()) {
            return null
        }

        return set
    }

    fun collectColumnNames(selectXmlTag: XmlTag?, project: Project): Set<String>? {
        val sqlExpressions = collectColumnNameElements(selectXmlTag, project)

        if (sqlExpressions == null) {
            return null
        }

        if (sqlExpressions.isEmpty()) {
            return null
        }

        val set = sqlExpressions
            .mapNotNull { it.name }
            .toSet()

        return set
    }

}

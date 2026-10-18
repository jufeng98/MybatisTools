package org.javamaster.mybatis.tools.utils

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
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
        if (!DatabasePluginUtils.databasePluginAvailable) {
            return emptySet()
        }

        val xmlTag = attribute.parent
        val parentXmlTag = xmlTag.parent
        if (parentXmlTag !is XmlTag) {
            return emptySet()
        }

        val domElement = DomUtil.findDomElement(attribute, IdDomElement::class.java) ?: return emptySet()

        val resultMapId = MapperUtils.getResultMapId(parentXmlTag) ?: return emptySet()

        val mapper = MapperUtils.getMapper(domElement)

        val project = parentXmlTag.project

        return mapper.selects
            .mapNotNull { it.xmlTag }
            .filter { it.getAttributeValue("resultMap") == resultMapId }
            .flatMap { collectColumnNameElements(it, project) }
            .toSet()
    }

    fun collectColumnNameElements(selectXmlTag: XmlTag?, project: Project): Set<SqlExpression> {
        if (selectXmlTag == null) {
            return emptySet()
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

        val sqlFile = injectedPsiFiles?.get(0)?.first as? SqlFile ?: return emptySet()

        val firstSelectClause = PsiTreeUtil.findChildOfType(sqlFile, SqlSelectClause::class.java) ?: return emptySet()

        val set = firstSelectClause.expressions
            .mapNotNull { it }
            .toSet()

        if (set.isEmpty()) {
            return emptySet()
        }

        return set
    }

    fun collectColumnNames(selectXmlTag: XmlTag?, project: Project): Set<String> {
        val sqlExpressions = collectColumnNameElements(selectXmlTag, project)

        val set = sqlExpressions
            .mapNotNull { it.name }
            .toSet()

        return set
    }

    fun getForeachTag(injectionHost: PsiElement): XmlTag? {
        val parent = injectionHost.parent
        if (parent is XmlTag) {
            val tagName = parent.name
            if ("foreach" == tagName) {
                return parent
            }

            val setTag = parent.parent
            if (setTag is XmlTag) {
                val foreachTag = setTag.parent
                if (foreachTag is XmlTag) {
                    if ("foreach" == foreachTag.name) {
                        return foreachTag
                    }
                }
            }
        } else if (parent is XmlAttribute) {
            val ifTag = parent.parent
            if (ifTag is XmlTag) {
                val setTag = ifTag.parent
                if (setTag is XmlTag) {
                    val foreachTag = setTag.parent
                    if (foreachTag is XmlTag) {
                        if ("foreach" == foreachTag.name) {
                            return foreachTag
                        }
                    }
                }
            }
        }

        return null
    }

}

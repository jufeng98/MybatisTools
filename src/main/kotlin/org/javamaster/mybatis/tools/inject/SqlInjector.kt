package org.javamaster.mybatis.tools.inject

import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlToken
import com.intellij.sql.psi.SqlLanguage
import org.apache.commons.lang3.StringUtils

/**
 * @author yudong
 */
object SqlInjector {

    fun startInjectSql(registrar: MultiHostRegistrar) {
        registrar.startInjecting(SqlLanguage.INSTANCE)
    }

    fun injectSqlLangIntoXmlText(
        registrar: MultiHostRegistrar,
        host: PsiLanguageInjectionHost,
        next: XmlText?,
        text: String,
        variablesRanges: List<TextRange>,
    ): List<TextRange> {
        val variablePrefix = calVariablePrefix(host, next)

        var lastVariableRangeEndOffset = 0

        for (variableRange in variablesRanges) {
            val range = TextRange.create(lastVariableRangeEndOffset, variableRange.startOffset)

            registrar.addPlace(variablePrefix, variableRange.substring(text), host, range)

            lastVariableRangeEndOffset = variableRange.endOffset
        }

        val range = TextRange.create(lastVariableRangeEndOffset, text.length)

        val suffix = calSqlSuffix(host)

        val prefix = calSqlPrefix(host)

        registrar.addPlace(prefix ?: variablePrefix, suffix, host, range.shiftRight(0))

        return variablesRanges
    }

    private fun calVariablePrefix(host: PsiLanguageInjectionHost, next: XmlText?): String? {
        val sibling = host.prevSibling.prevSibling
        val hostParent = host.parent
        if (sibling is XmlToken && hostParent is XmlTag) {
            val text = sibling.text
            if (text == "where") {
                val hostText = host.text
                if (StringUtils.isNotBlank(hostText)) {
                    return "where "
                }

                val nextText = next?.value
                if (nextText == null) {
                    return "where "
                }

                val str = nextText.trimStart().lowercase()
                if (!str.startsWith("and") && !str.startsWith("or")) {
                    return "where "
                }

                return "where 1 = 1 "
            } else if (text == "set") {
                return "set "
            } else {
                return null
            }
        }

        return null
    }

    private fun calSqlSuffix(host: PsiLanguageInjectionHost): String? {
        val nextSibling = host.nextSibling
        if (nextSibling is XmlTag) {
            if (nextSibling.name == "trim") {
                return nextSibling.getAttributeValue("prefix")
            }
        }

        return null
    }

    private fun calSqlPrefix(host: PsiLanguageInjectionHost): String? {
        val prevSibling = host.prevSibling
        if (prevSibling is XmlTag) {
            if (prevSibling.name == "trim") {
                return prevSibling.getAttributeValue("suffix")
            }
        }

        return null
    }

}

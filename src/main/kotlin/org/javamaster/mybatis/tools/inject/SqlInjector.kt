package org.javamaster.mybatis.tools.inject

import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlToken
import com.intellij.sql.psi.SqlLanguage
import kotlin.text.trimStart

object SqlInjector {

    fun startInject(registrar: MultiHostRegistrar) {
        registrar.startInjecting(SqlLanguage.INSTANCE)
    }

     fun injectSqlLangIntoXmlText(
         registrar: MultiHostRegistrar,
         host: PsiLanguageInjectionHost,
         next: XmlText?,
         text: String,
         variablesRanges: List<TextRange>,
    ): List<TextRange> {
        val sibling = host.prevSibling.prevSibling
        val prefix = if (sibling is XmlToken && host.parent is XmlTag && sibling.text == "where") {

            val siblingText = next?.text
            if (siblingText == null) {
                "where "
            } else {
                val str = siblingText.trimStart().lowercase()
                if (str.startsWith("and") || str.startsWith("or")) {
                    "where 1 = 1 "
                } else {
                    "where 1 = 1 and "
                }
            }
        } else {
            null
        }

        var lastVariableRangeEndOffset = 0

        for (variableRange in variablesRanges) {
            val range = TextRange.create(lastVariableRangeEndOffset, variableRange.startOffset)

            registrar.addPlace(prefix, variableRange.substring(text), host, range)

            lastVariableRangeEndOffset = variableRange.endOffset
        }

        val range = TextRange.create(lastVariableRangeEndOffset, text.length)

        registrar.addPlace(prefix, null, host, range.shiftRight(0))

        return variablesRanges
    }
}

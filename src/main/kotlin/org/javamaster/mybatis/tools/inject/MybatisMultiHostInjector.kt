package org.javamaster.mybatis.tools.inject

import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.ognl.OgnlLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlToken
import com.intellij.sql.psi.SqlLanguage
import com.intellij.util.SmartList
import org.javamaster.mybatis.tools.MybatisParamLanguage
import org.jetbrains.kotlin.idea.base.codeInsight.handlers.fixers.end
import org.jetbrains.kotlin.idea.base.codeInsight.handlers.fixers.start
import java.util.regex.Pattern

/**
 * 对 mybatis mapper xml 文件里的 #{} / ${} 等注入语言
 *
 * @author yudong
 */
class MybatisMultiHostInjector : MultiHostInjector {
    val sharpPrefixStr: String = "#{"
    val dollarPrefixStr: String = "\${"
    val pattern: Pattern = Pattern.compile("sql|select|insert|update|delete|statement")

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!MapperUtils.isElementWithinMybatisFile(context)) {
            return
        }

        val xmlTag = context as XmlTag

        val name = xmlTag.name
        if (!pattern.matcher(name).find()) {
            return
        }

        val xmlTexts = PsiTreeUtil.findChildrenOfType<XmlText>(xmlTag, XmlText::class.java)
        if (xmlTexts.isEmpty()) {
            return
        }

        val allParamRanges = mutableListOf<Pair<List<TextRange>, PsiElement>>()

        registrar.startInjecting(SqlLanguage.INSTANCE)

        for ((index, it) in xmlTexts.withIndex()) {
            val text = it.text
            val injectionHost = it as PsiLanguageInjectionHost

            val next = if (index < xmlTexts.size - 2) {
                xmlTexts.elementAt(index + 1)
            } else {
                null
            }


            val paramRanges = injectSqlLangIntoXmlText(registrar, injectionHost, next, text)

            allParamRanges.add(Pair(paramRanges, it))
        }

        registrar.doneInjecting()

        allParamRanges.forEach {
            val injectionHost = it.second as PsiLanguageInjectionHost

            injectLangToParams(it.first, registrar, injectionHost, injectionHost.text)
        }
    }

    private fun injectSqlLangIntoXmlText(
        registrar: MultiHostRegistrar,
        host: PsiLanguageInjectionHost,
        next: XmlText?,
        text: String,
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

        val variablesRanges = collectParamRanges(text)

        for (variableRange in variablesRanges) {
            val range = TextRange.create(lastVariableRangeEndOffset, variableRange.startOffset)

            registrar.addPlace(prefix, variableRange.substring(text), host, range)

            lastVariableRangeEndOffset = variableRange.endOffset
        }

        val range = TextRange.create(lastVariableRangeEndOffset, text.length)

        registrar.addPlace(prefix, null, host, range.shiftRight(0))

        return variablesRanges
    }

    private fun injectLangToParams(
        paramRanges: List<TextRange>,
        registrar: MultiHostRegistrar,
        injectionHost: PsiLanguageInjectionHost,
        text: String,
    ) {
        paramRanges.forEach {
            val paramStr = it.substring(text)
            val idx = paramStr.indexOf(",")
            if (idx == -1) {
                registrar.startInjecting(OgnlLanguage.INSTANCE)

                registrar.addPlace("%{", "}", injectionHost, it)

                registrar.doneInjecting()
            } else {
                val splitIndex = text.indexOf(",", it.start)

                registrar.startInjecting(OgnlLanguage.INSTANCE)

                val range1 = TextRange(it.start, splitIndex)
                registrar.addPlace("%{", "}", injectionHost, range1)

                registrar.doneInjecting()

                registrar.startInjecting(MybatisParamLanguage.INSTANCE)

                val range2 = TextRange(splitIndex, it.end)
                registrar.addPlace(null, null, injectionHost, range2)

                registrar.doneInjecting()
            }
        }
    }

    private fun collectParamRanges(text: String): List<TextRange> {
        var closeBraceIndex: Int
        var index = 0

        val textRanges = mutableListOf<TextRange>()

        while (index < text.length) {
            var openBraceIndex = text.indexOf(sharpPrefixStr, index)
            if (openBraceIndex == -1) {
                openBraceIndex = text.indexOf(dollarPrefixStr, index)
            }

            val actualStartIndex = openBraceIndex + 2
            closeBraceIndex = text.indexOf("}", actualStartIndex)
            if (openBraceIndex < 0 || closeBraceIndex < 0) {
                break
            }

            if (actualStartIndex >= closeBraceIndex) {
                continue
            }

            val paramStr = text.substring(actualStartIndex, closeBraceIndex)
            if (StringUtil.isEmptyOrSpaces(paramStr)) {
                continue
            }

            textRanges.add(TextRange.create(actualStartIndex, closeBraceIndex))

            index = closeBraceIndex + 1
        }

        return textRanges
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return SmartList(XmlTag::class.java)
    }
}

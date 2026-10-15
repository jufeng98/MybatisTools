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
import org.javamaster.mybatis.tools.MybatisParamLanguage
import org.javamaster.mybatis.tools.utils.DatabasePluginUtils.databasePluginAvailable
import kotlin.math.min

/**
 * 对 mybatis mapper xml 文件里的 #{} / ${} 等注入语言
 *
 * @author yudong
 */
class MybatisMultiHostInjector : MultiHostInjector {
    private val sharpPrefixStr: String = "#{"
    private val dollarPrefixStr: String = "\${"
    private val tagNames = setOf("sql", "select", "insert", "update", "delete", "statement")

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!MapperUtils.isElementWithinMybatisFile(context)) {
            return
        }

        val xmlTag = context as? XmlTag ?: return

        if (xmlTag.name !in tagNames) {
            return
        }

        val xmlTexts = PsiTreeUtil.findChildrenOfType<XmlText>(xmlTag, XmlText::class.java)
        if (xmlTexts.isEmpty()) {
            return
        }

        val allParamRanges = mutableListOf<Pair<List<TextRange>, XmlText>>()

        if (databasePluginAvailable) {
            SqlInjector.startInject(registrar)
        }

        xmlTexts.forEachIndexed { index, it ->
            val text = it.text

            val variablesRanges = collectParamRanges(text)

            if (databasePluginAvailable) {
                val injectionHost = it as PsiLanguageInjectionHost

                val next = xmlTexts.elementAtOrNull(index + 1)

                SqlInjector.injectSqlLangIntoXmlText(registrar, injectionHost, next, text, variablesRanges)
            }

            allParamRanges.add(Pair(variablesRanges, it))
        }

        if (databasePluginAvailable) {
            registrar.doneInjecting()
        }

        allParamRanges.forEach {
            val injectionHost = it.second as PsiLanguageInjectionHost

            injectLangToParams(it.first, registrar, injectionHost, injectionHost.text)
        }
    }

    private fun injectLangToParams(
        paramRanges: List<TextRange>,
        registrar: MultiHostRegistrar,
        injectionHost: PsiLanguageInjectionHost,
        text: String,
    ) {
        val length = injectionHost.textLength

        paramRanges.forEach {
            if (it.endOffset > length) {
                return@forEach
            }

            val idx = text.indexOf(",", it.startOffset)
            val splitIndex = if (idx != -1 && idx < it.endOffset) idx else -1

            if (splitIndex == -1) {
                registrar.startInjecting(OgnlLanguage.INSTANCE)
                registrar.addPlace("%{", "}", injectionHost, it)
                registrar.doneInjecting()
            } else {
                registrar.startInjecting(OgnlLanguage.INSTANCE)
                registrar.addPlace("%{", "}", injectionHost, TextRange(it.startOffset, splitIndex))
                registrar.doneInjecting()

                registrar.startInjecting(MybatisParamLanguage.INSTANCE)
                registrar.addPlace(null, null, injectionHost, TextRange(splitIndex, it.endOffset))
                registrar.doneInjecting()
            }
        }
    }

    private fun collectParamRanges(text: String): List<TextRange> {
        var index = 0

        val textRanges = mutableListOf<TextRange>()

        while (index < text.length) {
            val sharpBraceIndex = text.indexOf(sharpPrefixStr, index)
            val dollarBraceIndex = text.indexOf(dollarPrefixStr, index)

            val openBraceIndex = when {
                sharpBraceIndex == -1 -> dollarBraceIndex
                dollarBraceIndex == -1 -> sharpBraceIndex
                else -> min(sharpBraceIndex, dollarBraceIndex)
            }

            if (openBraceIndex < 0) {
                break
            }

            val actualStartIndex = openBraceIndex + 2
            val closeBraceIndex = text.indexOf("}", actualStartIndex)
            if (closeBraceIndex < 0) {
                break
            }

            if (actualStartIndex >= closeBraceIndex) {
                index = closeBraceIndex + 1
                continue
            }

            val paramStr = text.substring(actualStartIndex, closeBraceIndex)
            if (StringUtil.isEmptyOrSpaces(paramStr)) {
                index = closeBraceIndex + 1
                continue
            }

            textRanges.add(TextRange.create(actualStartIndex, closeBraceIndex))

            index = closeBraceIndex + 1
        }

        return textRanges
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return listOf(XmlTag::class.java)
    }

}

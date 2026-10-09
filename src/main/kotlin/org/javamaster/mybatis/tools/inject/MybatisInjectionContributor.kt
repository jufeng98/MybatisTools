package org.javamaster.mybatis.tools.inject

import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.xml.XmlText
import com.intellij.util.SmartList
import org.javamaster.mybatis.tools.MybatisParamLanguage

/**
 * 对 mybatis mapper xml 文件里的 #{} / ${} 注入语言
 *
 * @author yudong
 */
class MybatisInjectionContributor : MultiHostInjector {
    val sharpPrefixStr: String = "#{"
    val dollarPrefixStr: String = "\${"

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!MapperUtils.isElementWithinMybatisFile(context)) {
            return
        }

        val literalExpression = context

        val value = literalExpression.text

        registrar.startInjecting(MybatisParamLanguage.INSTANCE)

        injectXmlText(registrar, context as PsiLanguageInjectionHost, value)
    }

    private fun injectXmlText(
        registrar: MultiHostRegistrar,
        host: PsiLanguageInjectionHost,
        text: String,
    ) {
        val variablesRanges = collectVariablesRanges(text)
        if (variablesRanges.isEmpty()) {
            return
        }

        for (variableRange in variablesRanges) {
            registrar.startInjecting(MybatisParamLanguage.INSTANCE)

            registrar.addPlace(null, null, host, variableRange)
            
            registrar.doneInjecting()
        }

    }

    fun collectVariablesRanges(text: String): MutableList<TextRange> {
        val result = mutableListOf<TextRange>()

        var closeBraceIndex: Int
        var index = 0
        while (index < text.length) {
            var openBraceIndex = text.indexOf(sharpPrefixStr, index)
            if (openBraceIndex == -1) {
                openBraceIndex = text.indexOf(dollarPrefixStr, index)
            }

            closeBraceIndex = text.indexOf("}", openBraceIndex + 2)
            if (openBraceIndex < 0 || closeBraceIndex < 0) {
                break
            }

            val range = TextRange.create(openBraceIndex + 2, closeBraceIndex)
            if (!range.isEmpty && !StringUtil.isEmptyOrSpaces(text.substring(openBraceIndex + 2, closeBraceIndex))) {
                result.add(range)
            }
            index = closeBraceIndex + 1
        }

        return result
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return SmartList(XmlText::class.java)
    }
}

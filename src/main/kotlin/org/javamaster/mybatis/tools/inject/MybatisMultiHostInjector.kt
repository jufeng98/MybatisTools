package org.javamaster.mybatis.tools.inject

import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.baomidou.plugin.idea.mybatisx.util.MybatisConstants
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
import org.javamaster.mybatis.tools.utils.MapperUtil
import org.javamaster.mybatis.tools.utils.PsiUtils
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

        val mapper = MapperUtil.getMapper(xmlTag)

        injectXmlTag(xmlTag, mapper, registrar)
    }

    private fun injectXmlTag(xmlTag: XmlTag, mapper: Mapper?, registrar: MultiHostRegistrar) {
        val xmlTexts = PsiTreeUtil.findChildrenOfType<XmlText>(xmlTag, XmlText::class.java)
        if (xmlTexts.isEmpty()) {
            return
        }

        val allMyBatisParams = mutableListOf<Pair<List<MyBatisParam>, XmlText>>()
        val otherMyBatisParams = mutableListOf<Pair<List<MyBatisParam>, XmlText>>()

        try {
            if (databasePluginAvailable) {
                SqlInjector.startInjectSql(registrar)
            }

            val visited = mutableSetOf<String>()

            injectXmlTexts(xmlTexts, mapper, allMyBatisParams, otherMyBatisParams, visited, registrar)
        } finally {
            if (databasePluginAvailable) {
                registrar.doneInjecting()
            }
        }

        allMyBatisParams.forEach {
            val paramRanges = it.first
            if (paramRanges.isEmpty()) {
                return@forEach
            }

            val injectionHost = it.second as PsiLanguageInjectionHost

            injectLangToParams(paramRanges, registrar, injectionHost)
        }
    }

    private fun injectXmlTexts(
        xmlTexts: Collection<XmlText>,
        mapper: Mapper?,
        allMyBatisParams: MutableList<Pair<List<MyBatisParam>, XmlText>>,
        otherMyBatisParams: MutableList<Pair<List<MyBatisParam>, XmlText>>,
        visited: MutableSet<String>,
        registrar: MultiHostRegistrar,
    ) {
        xmlTexts.forEachIndexed { index, it ->
            val text = it.text

            val variablesRanges = collectParamRanges(text)

            allMyBatisParams.add(Pair(variablesRanges, it))

            if (!databasePluginAvailable) {
                return@forEachIndexed
            }

            val next = PsiUtils.getNextNotBlankXmlText(xmlTexts, index + 1)

            SqlInjector.injectSqlLangIntoXmlText(registrar, it as PsiLanguageInjectionHost, next, text, variablesRanges)

            val nextSibling = it.nextSibling
            if (nextSibling is XmlTag) {
                if (nextSibling.name == "include") {
                    injectIncludeRelateSqlTag(nextSibling, mapper, otherMyBatisParams, visited, registrar)
                }
            }
        }
    }

    private fun injectIncludeRelateSqlTag(
        includeXmlTag: XmlTag,
        mapper: Mapper?,
        otherMyBatisParams: MutableList<Pair<List<MyBatisParam>, XmlText>>,
        visited: MutableSet<String>,
        registrar: MultiHostRegistrar,
    ) {
        mapper ?: return

        val refId = includeXmlTag.getAttributeValue("refid")

        if (refId.isNullOrBlank()) return

        if (!visited.add(refId)) return   // 已访问过，跳过

        val sqlXmlTag = mapper.sqls
            .mapNotNull { it.xmlTag }
            .firstOrNull { it.getAttributeValue("id") == refId }

        if (sqlXmlTag == null) return

        val sqlXmlTexts = PsiTreeUtil.findChildrenOfType(sqlXmlTag, XmlText::class.java)
        if (sqlXmlTexts.isEmpty()) {
            return
        }

        injectXmlTexts(sqlXmlTexts, mapper, otherMyBatisParams, mutableListOf(), visited, registrar)
    }

    private fun injectLangToParams(
        myBatisParams: List<MyBatisParam>,
        registrar: MultiHostRegistrar,
        injectionHost: PsiLanguageInjectionHost,
    ) {
        val length = injectionHost.textLength

        myBatisParams.forEach { param ->
            if (param.nameRange.endOffset > length) return@forEach

            // 参数名部分：注入 OGNL
            registrar.startInjecting(OgnlLanguage.INSTANCE)
            registrar.addPlace("%{", "}", injectionHost, param.nameRange)
            registrar.doneInjecting()

            // 逗号之后的部分：注入 MybatisParamLanguage
            val suffix = param.suffixRange ?: return@forEach
            if (suffix.endOffset > length) return@forEach

            registrar.startInjecting(MybatisParamLanguage.INSTANCE)
            registrar.addPlace(null, null, injectionHost, suffix)
            registrar.doneInjecting()
        }
    }

    private fun collectParamRanges(text: String): List<MyBatisParam> {
        val myBatisParams = mutableListOf<MyBatisParam>()
        var index = 0

        while (index < text.length) {
            val sharpBraceIndex = text.indexOf(sharpPrefixStr, index)
            val dollarBraceIndex = text.indexOf(dollarPrefixStr, index)

            val openBraceIndex = when {
                sharpBraceIndex == -1 -> dollarBraceIndex
                dollarBraceIndex == -1 -> sharpBraceIndex
                else -> min(sharpBraceIndex, dollarBraceIndex)
            }

            if (openBraceIndex < 0) break

            val contentStart = openBraceIndex + 2
            val closeBraceIndex = text.indexOf("}", contentStart)
            if (closeBraceIndex < 0) break

            // #{} / ${} 空内容
            if (contentStart >= closeBraceIndex) {
                index = closeBraceIndex + 1
                continue
            }

            val content = text.substring(contentStart, closeBraceIndex)
            if (StringUtil.isEmptyOrSpaces(content)) {
                index = closeBraceIndex + 1
                continue
            }

            // 关键：只在当前参数的 content 范围内找 @ 和 ,
            // 原实现从 startOffset 一直往后 indexOf，会把下一个参数里的 @ 误判进来
            val atIndex = text.indexOf(MybatisConstants.AT_SEPARATOR, contentStart)
                .takeIf { it in contentStart until closeBraceIndex } ?: -1
            val commaIndex = text.indexOf(",", contentStart)
                .takeIf { it in contentStart until closeBraceIndex } ?: -1

            val param = when {
                // @ 出现在第一个逗号之前（或没有逗号）→ 整个 content 是静态方法调用
                atIndex != -1 && (commaIndex == -1 || atIndex < commaIndex) ->
                    MyBatisParam(
                        nameRange = TextRange.create(contentStart, closeBraceIndex),
                        suffixRange = null,
                    )

                // 有逗号 → 逗号前是 OGNL，逗号后（含逗号）是 jdbcType/typeHandler 等
                commaIndex != -1 ->
                    MyBatisParam(
                        nameRange = TextRange.create(contentStart, commaIndex),
                        suffixRange = TextRange.create(commaIndex, closeBraceIndex),
                    )

                // 纯参数名
                else ->
                    MyBatisParam(
                        nameRange = TextRange.create(contentStart, closeBraceIndex),
                        suffixRange = null,
                    )
            }

            myBatisParams.add(param)
            index = closeBraceIndex + 1
        }

        return myBatisParams
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return listOf(XmlTag::class.java)
    }

}

data class MyBatisParam(
    /** 参数名部分，注入 OGNL；也是 addPlace 的 place 范围 */
    val nameRange: TextRange,
    /** 逗号之后的 jdbcType/typeHandler 等部分，注入 MybatisParamLanguage；没有则为 null */
    val suffixRange: TextRange?,
)

package org.javamaster.mybatis.tools.provider

import com.baomidou.plugin.idea.mybatisx.dom.model.IdDomElement
import com.baomidou.plugin.idea.mybatisx.tip.hashmark.CompositeHashMarkTip
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.util.ProcessingContext
import com.intellij.util.xml.DomUtil
import org.javamaster.mybatis.tools.psi.MybatisAttr
import org.javamaster.mybatis.tools.psi.MybatisAttrValue

class MybatisAttrValueCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        completionParameters: CompletionParameters,
        processingContext: ProcessingContext,
        completionResultSet: CompletionResultSet,
    ) {
        val position = completionParameters.position
        val psiElement = position.parent ?: return

        val mybatisAttrValue = psiElement as MybatisAttrValue
        val mybatisAttr = mybatisAttrValue.parent as MybatisAttr

        val injectionHost = InjectedLanguageManager.getInstance(mybatisAttr.project).getInjectionHost(mybatisAttrValue)
        val domElement = DomUtil.findDomElement(injectionHost, IdDomElement::class.java) ?: return
        val mapper = MapperUtils.getMapper(domElement)

        CompositeHashMarkTip.hashMarkTips
            .filter {
                it.name == mybatisAttr.attrName.name
            }
            .forEach {
                it.tipValue(completionParameters, completionResultSet, mapper)
            }
    }

}
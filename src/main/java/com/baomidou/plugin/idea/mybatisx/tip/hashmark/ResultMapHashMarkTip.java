package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.baomidou.plugin.idea.mybatisx.dom.model.ResultMap;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import org.javamaster.mybatis.tools.psi.MybatisParamAttrValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 提示 resultMap
 *
 * @author ls9527
 */
public class ResultMapHashMarkTip implements HashMarkTip {

    @Override
    public String getName() {
        return "resultMap";
    }

    /**
     * resultMap 标签太多，只提示当前文件的
     */
    @Override
    public void tipValue(CompletionParameters parameters, CompletionResultSet completionResultSet, Mapper mapper) {
        CompletionResultSet caseInsensitiveResultSet = completionResultSet.caseInsensitive();
        for (ResultMap resultMap : mapper.getResultMaps()) {
            caseInsensitiveResultSet.addElement(LookupElementBuilder.create(resultMap.getId()));
        }
    }

    @Override
    public @Nullable PsiElement resolveValue(@NotNull MybatisParamAttrValue paramAttrValue, @NotNull Mapper mapper) {
        String name = paramAttrValue.getName();

        for (ResultMap resultMap : mapper.getResultMaps()) {
            XmlAttributeValue xmlAttributeValue = resultMap.getId().getXmlAttributeValue();
            if (xmlAttributeValue == null) {
                continue;
            }

            if (name.equals(xmlAttributeValue.getValue())) {
                return xmlAttributeValue;
            }
        }

        return null;
    }
}

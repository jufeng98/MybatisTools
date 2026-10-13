package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import org.javamaster.mybatis.tools.psi.MybatisParamAttrValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NumericScaleHashMarkTip implements HashMarkTip {
    @Override
    public String getName() {
        return "numericScale";
    }

    /**
     * 是数字,可以不用提示。<br/>
     * 提示默认以常见的保留2位和4位小数
     */
    @Override
    public void tipValue(CompletionParameters parameters, CompletionResultSet completionResultSet, Mapper mapper) {
        completionResultSet.addElement(LookupElementBuilder.create("2"));
        completionResultSet.addElement(LookupElementBuilder.create("4"));
    }

    @Override
    public @Nullable PsiElement resolveValue(@NotNull MybatisParamAttrValue paramAttrValue, @NotNull Mapper mapper) {
        return null;
    }
}

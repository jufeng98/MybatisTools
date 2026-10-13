package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import org.javamaster.mybatis.tools.psi.MybatisParamAttrValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JdbcTypeNameHashMarkTip implements HashMarkTip {
    @Override
    public String getName() {
        return "jdbcTypeName";
    }

    /**
     * 自定义结构体类型,无需支持
     */
    @Override
    public void tipValue(CompletionParameters parameters, CompletionResultSet completionResultSet, Mapper mapper) {

    }

    @Override
    public @Nullable PsiElement resolveValue(@NotNull MybatisParamAttrValue paramAttrValue, @NotNull Mapper mapper) {
        return null;
    }
}

package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;

public interface HashMarkTip {
    String getName();

    void tipValue(CompletionParameters parameters, CompletionResultSet completionResultSet, Mapper mapper);
}

package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import lombok.val;

public class JavaTypeHashMark implements HashMarkTip {
    @Override
    public String getName() {
        return "javaType";
    }

    @Override
    public void tipValue(CompletionParameters parameters, CompletionResultSet completionResultSet, Mapper mapper) {
        val newResult = completionResultSet.withPrefixMatcher(CompletionUtil.findReferenceOrAlphanumericPrefix(parameters));

        newResult.addElement(LookupElementBuilder.create("String"));
        newResult.addElement(LookupElementBuilder.create("int"));
        newResult.addElement(LookupElementBuilder.create("float"));
        newResult.addElement(LookupElementBuilder.create("double"));
        newResult.addElement(LookupElementBuilder.create("long"));
        newResult.addElement(LookupElementBuilder.create("ResultSet"));
        newResult.addElement(LookupElementBuilder.create("ArrayList"));
        newResult.addElement(LookupElementBuilder.create("hashmap"));

        JavaClassNameCompletionContributor.addAllClasses(
                parameters,
                parameters.getInvocationCount() <= 1,
                newResult.getPrefixMatcher(),
                newResult
        );
    }
}

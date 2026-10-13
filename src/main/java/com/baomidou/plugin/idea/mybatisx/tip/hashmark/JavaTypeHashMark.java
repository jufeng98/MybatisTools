package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import lombok.val;
import org.javamaster.mybatis.tools.psi.MybatisParamAttrValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public @Nullable PsiElement resolveValue(@NotNull MybatisParamAttrValue paramAttrValue, @NotNull Mapper mapper) {
        val project = paramAttrValue.getProject();
        String name = paramAttrValue.getText();
        GlobalSearchScope searchScope = GlobalSearchScope.everythingScope(project);

        val psiClass = JavaPsiFacade.getInstance(project).findClass(name, searchScope);
        if (psiClass != null) {
            return psiClass;
        }

        @NotNull PsiClass[] psiClasses = PsiShortNamesCache.getInstance(project).getClassesByName(name, searchScope);
        if (psiClasses.length > 0) {
            return psiClasses[0];
        }

        return null;
    }
}

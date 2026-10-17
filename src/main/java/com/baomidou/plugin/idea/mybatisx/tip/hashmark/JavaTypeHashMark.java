package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.alias.AliasFacade;
import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.val;
import org.javamaster.mybatis.tools.psi.MybatisParamAttrValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JavaTypeHashMark implements HashMarkTip {
    @Override
    public String getName() {
        return "javaType";
    }

    @Override
    public void tipValue(CompletionParameters parameters, CompletionResultSet completionResultSet, Mapper mapper) {
        val newResult = completionResultSet.withPrefixMatcher(CompletionUtil.findReferenceOrAlphanumericPrefix(parameters));

        PsiElement position = parameters.getPosition();

        AliasFacade aliasFacade = AliasFacade.getInstance(position.getProject());

        List<LookupElementBuilder> elements = aliasFacade.getAliasElements(position);

        elements.forEach(newResult::addElement);

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

        AliasFacade aliasFacade = AliasFacade.getInstance(project);

        return aliasFacade.findPsiClass(paramAttrValue, name).orElse(null);
    }
}

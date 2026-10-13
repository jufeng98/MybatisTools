package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.alias.AliasDesc;
import com.baomidou.plugin.idea.mybatisx.alias.AliasFacade;
import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.completion.JavaClassNameCompletionContributor;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.val;
import org.javamaster.mybatis.tools.psi.MybatisParamAttrValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class JavaTypeHashMark implements HashMarkTip {
    @Override
    public String getName() {
        return "javaType";
    }

    @Override
    public void tipValue(CompletionParameters parameters, CompletionResultSet completionResultSet, Mapper mapper) {
        val newResult = completionResultSet.withPrefixMatcher(CompletionUtil.findReferenceOrAlphanumericPrefix(parameters));

        AliasFacade aliasFacade = AliasFacade.getInstance(parameters.getPosition().getProject());

        Collection<AliasDesc> aliasDescList = aliasFacade.getAliasDescs(parameters.getPosition());
        for (AliasDesc aliasDesc : aliasDescList) {
            PsiClass psiClass = aliasDesc.getClazz();

            LookupElementBuilder lookupElement = LookupElementBuilder
                    .create(psiClass, aliasDesc.getAlias())
                    .withTypeText(psiClass.getQualifiedName());

            newResult.addElement(lookupElement);
        }

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

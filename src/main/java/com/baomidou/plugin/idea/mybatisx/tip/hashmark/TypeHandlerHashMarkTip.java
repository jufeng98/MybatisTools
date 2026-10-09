package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import com.baomidou.plugin.idea.mybatisx.dom.model.Mapper;
import com.baomidou.plugin.idea.mybatisx.util.JavaUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;

import java.util.Collection;
import java.util.Optional;

/**
 * 类型处理器的提示
 *
 * @author ls9527
 */
public class TypeHandlerHashMarkTip implements HashMarkTip {

    private static final String ORG_APACHE_IBATIS_TYPE_TYPE_HANDLER = "org.apache.ibatis.type.TypeHandler";

    @Override
    public String getName() {
        return "typeHandler";
    }

    /**
     * 通常希望提示自定义的类型处理器
     */
    @Override
    public void tipValue(CompletionParameters parameters, CompletionResultSet completionResultSet, Mapper mapper) {
        if (mapper.getXmlTag() == null) {
            return;
        }

        Project project = mapper.getXmlTag().getProject();
        Optional<PsiClass> typeHandlerOptional = JavaUtils.findClazz(project, ORG_APACHE_IBATIS_TYPE_TYPE_HANDLER);
        if (typeHandlerOptional.isEmpty()) {
            return;
        }

        PsiClass typeHandler = typeHandlerOptional.get();

        Query<PsiClass> search = ClassInheritorsSearch.search(typeHandler, true);
        Collection<PsiClass> all = search.findAll();
        for (PsiClass typeHandlerImplClass : all) {
            if (typeHandlerImplClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
                continue;
            }

            if (typeHandlerImplClass.getQualifiedName() == null) {
                continue;
            }

            JavaPsiClassReferenceElement item = JavaClassNameCompletionContributor.createClassLookupItem(typeHandlerImplClass, false);
            completionResultSet.addElement(item);
        }
    }
}

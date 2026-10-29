package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.baomidou.plugin.idea.mybatisx.dom.MapperBacktrackingUtils;
import com.baomidou.plugin.idea.mybatisx.setting.MybatisXSettings;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import org.javamaster.mybatis.tools.utils.ReferenceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PropertyFind {

    public static Optional<PsiMember> getStartElement(@Nullable String firstText, XmlAttributeValue element) {
        if (firstText == null) {
            return Optional.empty();
        }

        Optional<PsiClass> clazz = MapperBacktrackingUtils.getPropertyClazz(element);
        if (clazz.isEmpty()) {
            return Optional.empty();
        }

        PsiClass psiClass = clazz.get();

        return getPsiMember(firstText, psiClass);
    }

    private static Optional<PsiMember> getPsiMember(@NotNull String firstText, PsiClass psiClass) {
        if (psiClass.isAnnotationType() || psiClass.isInterface()) {
            return Optional.empty();
        }

        PsiField propertyField = PropertyUtil.findPropertyField(psiClass, firstText, false);

        Boolean forceFindField = MybatisXSettings.getInstance().getForceFindField();
        if (forceFindField) {
            return Optional.ofNullable(propertyField);
        }

        if (propertyField != null) {
            return Optional.of(propertyField);
        }

        PsiMethod psiMethod = ReferenceUtils.INSTANCE.findFieldPossibleMethod(psiClass, firstText);

        return Optional.ofNullable(psiMethod);
    }

}

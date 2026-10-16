package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.baomidou.plugin.idea.mybatisx.dom.MapperBacktrackingUtils;
import com.baomidou.plugin.idea.mybatisx.setting.MybatisXSettings;
import com.baomidou.plugin.idea.mybatisx.util.StringUtils;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class PropertySetterFind {

    public Optional<PsiField> getStartElement(@Nullable String firstText, XmlAttributeValue element) {
        if (firstText == null) {
            return Optional.empty();
        }

        Optional<PsiClass> clazz = MapperBacktrackingUtils.getPropertyClazz(element);
        if (clazz.isEmpty()) {
            return Optional.empty();
        }

        return getPsiField(firstText, clazz.get());
    }

    public List<PsiField> getSetterFields(PsiClass psiClass) {
        return Arrays.stream(psiClass.getAllFields())
                .filter(field -> getPsiField(field, psiClass).isPresent())
                .collect(Collectors.toList());
    }

    private Optional<PsiField> getPsiField(@NotNull PsiField psiField, PsiClass psiClass) {
        return getPsiField(psiField.getName(), psiClass);
    }

    private Optional<PsiField> getPsiField(@NotNull String firstText, PsiClass psiClass) {
        if (psiClass.isAnnotationType() || psiClass.isInterface()) {
            return Optional.empty();
        }

        Boolean forceFindField = MybatisXSettings.getInstance().getForceFindField();
        if (forceFindField) {
            PsiField propertyField = PropertyUtil.findPropertyField(psiClass, firstText, false);

            return Optional.ofNullable(propertyField);
        }

        PsiMethod propertySetter = PropertyUtil.findPropertySetter(psiClass, firstText, false, true);
        if (null == propertySetter) {
            propertySetter = findPsiMethodIfNotSetter(firstText, psiClass);
        }

        PsiField propertyField = PropertyUtil.findPropertyField(psiClass, firstText, false);

        return propertySetter == null ? Optional.empty() : Optional.ofNullable(propertyField);
    }

    /**
     * 支持特殊的 setter 方法
     * 例如 oName, 正常来说： 生成的方法名应该是: setoName, 部分框架会生成: setOName
     * 而mybatis是支持这两种写法的
     */
    private PsiMethod findPsiMethodIfNotSetter(@NotNull String firstText, PsiClass psiClass) {
        String setterMethodName = "set" + StringUtils.upperCaseFirstChar(firstText);
        final PsiMethod[] methodsByName = psiClass.findMethodsByName(setterMethodName, true);
        for (PsiMethod psiMethod : methodsByName) {
            if (psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
                continue;
            }

            return psiMethod;
        }

        return null;
    }

}

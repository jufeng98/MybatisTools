package com.baomidou.plugin.idea.mybatisx.smartjpa.util;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * The type Field util.
 *
 * @author ls9527
 */
public class FieldUtil {
    /**
     * 根据class 获取字段的名称
     *
     * @param entityClass the entity class
     * @return string psi field map
     */
    @NotNull
    public static Map<String, PsiField> getStringPsiFieldMap(PsiClass entityClass) {
        return Arrays.stream(entityClass.getAllFields())
                .filter(field -> (!field.hasModifierProperty(PsiModifier.STATIC))
                        && (!field.hasModifierProperty(PsiModifier.TRANSIENT)))
                .collect(Collectors.toMap(PsiField::getName, x -> x, BinaryOperator.maxBy(Comparator.comparing(PsiField::getName))));
    }

    /**
     * Gets psi field list.
     *
     * @param entityClass the entity class
     * @return the psi field list
     */
    @NotNull
    public static List<PsiField> getPsiFieldList(PsiClass entityClass) {
        return Arrays.stream(entityClass.getAllFields())
                .filter(field -> (!field.hasModifierProperty(PsiModifier.STATIC))
                        && (!field.hasModifierProperty(PsiModifier.TRANSIENT)))
                .collect(Collectors.toList());
    }

}

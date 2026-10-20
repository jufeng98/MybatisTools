package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.*;

/**
 * @author yudong
 */
public class ResultMapIdConverter extends Converter<Object> implements CustomReferenceConverter<Object> {

    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<Object> genericDomValue,
                                                     PsiElement psiElement,
                                                     ConvertContext convertContext) {
        return PsiReference.EMPTY_ARRAY;
    }

    @Override
    public @Nullable Object fromString(@Nullable @NonNls String s, @NotNull ConvertContext convertContext) {
        return convertContext.getInvocationElement().getXmlElement();
    }

    @Override
    public @Nullable String toString(@Nullable Object o, @NotNull ConvertContext convertContext) {
        return null;
    }

}

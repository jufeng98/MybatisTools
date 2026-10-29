package com.baomidou.plugin.idea.mybatisx.reference;

import com.baomidou.plugin.idea.mybatisx.dom.converter.PropertyFind;
import com.baomidou.plugin.idea.mybatisx.util.JavaUtils;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The type Psi field reference set resolver.
 *
 * @author yanglin
 */
public class PsiFieldReferenceSetResolver extends ContextReferenceSetResolver<XmlAttributeValue, PsiMember> {

    /**
     * Instantiates a new Psi field reference set resolver.
     *
     * @param from the from
     */
    protected PsiFieldReferenceSetResolver(XmlAttributeValue from) {
        super(from);
    }

    @NotNull
    @Override
    public String getText() {
        return getElement().getValue();
    }

    @NotNull
    @Override
    public Optional<PsiMember> resolve(@NotNull PsiMember current, String text) {
        if (current instanceof PsiField psiField) {
            PsiType type = psiField.getType();

            if (type instanceof PsiClassReferenceType && !((PsiClassReferenceType) type).hasParameters()) {
                PsiClass clazz = ((PsiClassReferenceType) type).resolve();
                if (clazz != null) {
                    return JavaUtils.findPsiMember(clazz, text);
                }
            }

            return Optional.empty();
        }

        return Optional.of(current);
    }

    @NotNull
    @Override
    public Optional<PsiMember> getStartElement(@Nullable String firstText) {
        return PropertyFind.getStartElement(firstText, getElement());
    }

}

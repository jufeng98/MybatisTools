package com.baomidou.plugin.idea.mybatisx.alias;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The type Alias class reference.
 *
 * @author yanglin
 */
public class AliasClassReference extends PsiReferenceBase<XmlAttributeValue> {

    /**
     * Instantiates a new Alias class reference.
     *
     * @param element the element
     */
    public AliasClassReference(@NotNull XmlAttributeValue element) {
        super(element, true);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        XmlAttributeValue attributeValue = getElement();

        AliasFacade aliasFacade = AliasFacade.getInstance(attributeValue.getProject());

        return aliasFacade.findPsiClass(attributeValue, attributeValue.getValue()).orElse(null);
    }

    @Override
    public Object @NotNull [] getVariants() {
        XmlAttributeValue attributeValue = getElement();

        AliasFacade aliasFacade = AliasFacade.getInstance(attributeValue.getProject());

        List<LookupElementBuilder> elements = aliasFacade.getAliasElements(attributeValue);

        return elements.toArray(LookupElementBuilder[]::new);
    }
}

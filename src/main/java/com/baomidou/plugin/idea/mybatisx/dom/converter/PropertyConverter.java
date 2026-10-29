package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.baomidou.plugin.idea.mybatisx.reference.ResultPropertyReferenceSet;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.*;

import java.util.Optional;

/**
 * The type Property converter.
 *
 * @author yanglin
 */
public class PropertyConverter extends ConverterAdaptor<XmlAttributeValue> implements CustomReferenceConverter<XmlAttributeValue> {

    @Override
    public String getErrorMessage(@Nullable String property, @NotNull ConvertContext context) {
        return "property (" + property + ") can not found or no getter method";
    }


    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<XmlAttributeValue> value, PsiElement element, ConvertContext context) {
        final String fieldName = value.getStringValue();
        if (fieldName == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        int offsetInElement = ElementManipulators.getOffsetInElement(element);
        ResultPropertyReferenceSet referenceSet = new ResultPropertyReferenceSet(fieldName, element, offsetInElement);
        return referenceSet.getPsiReferences();
    }

    @Nullable
    @Override
    public XmlAttributeValue fromString(@Nullable @NonNls String firstText, @NotNull ConvertContext context) {
        DomElement ctxElement = context.getInvocationElement();
        if (!(ctxElement instanceof GenericAttributeValue<?> attributeValue)) {
            return null;
        }

        final XmlAttributeValue xmlAttributeValue = attributeValue.getXmlAttributeValue();
        final Optional<PsiMember> startElement = PropertyFind.getStartElement(firstText, xmlAttributeValue);
        if (startElement.isEmpty()) {
            return null;
        }

        return xmlAttributeValue;
    }

}

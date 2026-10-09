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
        return "property (" + property + ") can not found setter method";
    }


    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<XmlAttributeValue> value, PsiElement element, ConvertContext context) {
        final String fieldName = value.getStringValue();
        if (fieldName == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        return new ResultPropertyReferenceSet(fieldName, element, ElementManipulators.getOffsetInElement(element)).getPsiReferences();
    }

    @Nullable
    @Override
    public XmlAttributeValue fromString(@Nullable @NonNls String firstText, @NotNull ConvertContext context) {
        DomElement ctxElement = context.getInvocationElement();
        if (ctxElement instanceof GenericAttributeValue<?> attributeValue) {
            final XmlAttributeValue xmlAttributeValue = attributeValue.getXmlAttributeValue();
            PropertySetterFind propertySetterFind = new PropertySetterFind();
            final Optional<PsiField> startElement = propertySetterFind.getStartElement(firstText, xmlAttributeValue);
            if (startElement.isPresent()) {
                return xmlAttributeValue;
            }
        }
        return null;
    }

}

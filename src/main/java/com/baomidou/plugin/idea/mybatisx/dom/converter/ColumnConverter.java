package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.baomidou.plugin.idea.mybatisx.reference.ResultColumnReferenceSet;
import com.baomidou.plugin.idea.mybatisx.util.*;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;
import com.intellij.util.xml.*;
import org.javamaster.mybatis.tools.utils.DatabasePluginUtils;
import org.jetbrains.annotations.*;

import java.util.Optional;

/**
 * The type Column converter.
 *
 * @author ls9527
 */
public class ColumnConverter extends ConverterAdaptor<XmlAttributeValue> implements CustomReferenceConverter<XmlAttributeValue> {

    public static final String NAMESPACE = "namespace";


    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<XmlAttributeValue> value, PsiElement element, ConvertContext context) {
        String stringValue = value.getStringValue();
        if (stringValue == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        // 社区版就不需要跳转到数据库的列了
        if (!DatabasePluginUtils.INSTANCE.getDatabasePluginAvailable()) {
            return PsiReference.EMPTY_ARRAY;
        }

        int offsetInElement = ElementManipulators.getOffsetInElement(element);

        Optional<PsiClass> mapperClassOptional = findMapperClass(context);
        PsiClass mapperClass = mapperClassOptional.orElse(null);

        return new ResultColumnReferenceSet(stringValue, element, offsetInElement, mapperClass).getPsiReferences();
    }

    @Nullable
    @Override
    public XmlAttributeValue fromString(@Nullable @NonNls String s, @NotNull ConvertContext context) {
        DomElement ctxElement = context.getInvocationElement();

        return ctxElement instanceof GenericAttributeValue ? ((GenericAttributeValue<?>) ctxElement).getXmlAttributeValue() : null;
    }

    private Optional<PsiClass> findMapperClass(ConvertContext context) {
        XmlTag rootTag = context.getFile().getRootTag();
        if (rootTag == null) {
            return Optional.empty();
        }

        XmlAttribute namespace = rootTag.getAttribute(NAMESPACE);
        if (namespace == null) {
            return Optional.empty();
        }

        String value = namespace.getValue();
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }

        return JavaUtils.findClazz(context.getProject(), value);
    }

}

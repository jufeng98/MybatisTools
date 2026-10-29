package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.xml.*;
import org.javamaster.mybatis.tools.utils.OgnlReferenceUtils;
import org.jetbrains.annotations.*;

/**
 * @author yudong
 */
public class BindNameConverter extends ConverterAdaptor<PsiElement> implements CustomReferenceConverter<PsiElement> {

    @Nullable
    @Override
    public PsiElement fromString(@Nullable @NonNls String s, @NotNull ConvertContext context) {
        if (StringUtil.isEmptyOrSpaces(s)) {
            return null;
        }

        XmlElement xmlElement = context.getXmlElement();
        XmlTag xmlTag = PsiTreeUtil.getParentOfType(xmlElement, XmlTag.class);
        if (xmlTag == null) {
            return null;
        }

        XmlAttribute valueAttr = xmlTag.getAttribute("value");
        if (valueAttr == null) {
            return null;
        }

        XmlAttributeValue valueElement = valueAttr.getValueElement();
        if (valueElement == null) {
            return null;
        }

        return OgnlReferenceUtils.INSTANCE.resolveBindValueAttr(valueElement);
    }


    @Nullable
    @Override
    public String toString(@Nullable PsiElement psiElement, @NotNull ConvertContext context) {
        return null;
    }


    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<PsiElement> value, PsiElement element, ConvertContext context) {
        return PsiReference.EMPTY_ARRAY;
    }

}

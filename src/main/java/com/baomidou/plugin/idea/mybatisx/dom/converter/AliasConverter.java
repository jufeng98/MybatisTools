package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.baomidou.plugin.idea.mybatisx.alias.AliasClassReference;
import com.baomidou.plugin.idea.mybatisx.alias.AliasFacade;
import com.baomidou.plugin.idea.mybatisx.util.MybatisConstants;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.*;

/**
 * The type Alias converter.
 *
 * @author yanglin
 */
public class AliasConverter extends ConverterAdaptor<PsiClass> implements CustomReferenceConverter<PsiClass> {

    private final PsiClassConverter delegate = new PsiClassConverter();

    @Nullable
    @Override
    public PsiClass fromString(@Nullable @NonNls String s, @NotNull ConvertContext context) {
        if (StringUtil.isEmptyOrSpaces(s)) {
            return null;
        }

        // 识别别名
        if (!s.contains(MybatisConstants.DOT_SEPARATOR)) {
            return AliasFacade.getInstance(context.getProject())
                    .findPsiClass(context.getXmlElement(), s)
                    .orElse(null);
        }

        // 根据全程查找
        return DomJavaUtil.findClass(s, context.getFile(), context.getModule(), GlobalSearchScope.allScope(context.getProject()));
    }


    @Nullable
    @Override
    public String toString(@Nullable PsiClass psiClass, @NotNull ConvertContext context) {
        return delegate.toString(psiClass, context);
    }


    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<PsiClass> value, PsiElement element, ConvertContext context) {
        if (notAlias((XmlAttributeValue) element)) {
            return delegate.createReferences(value, element, context);
        } else {
            return new PsiReference[]{new AliasClassReference((XmlAttributeValue) element)};
        }
    }

    private boolean notAlias(XmlAttributeValue element) {
        return element.getValue().contains(MybatisConstants.DOT_SEPARATOR);
    }


}

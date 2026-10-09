package com.intellij.lang.ognl;

import com.baomidou.plugin.idea.mybatisx.util.MapperUtils;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.patterns.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author yudong
 */
public class OgnlMultiHostInjector implements MultiHostInjector {
    public static final PsiElementPattern.Capture<XmlAttributeValue> TEST_ATTRIBUTE_VALUE = PlatformPatterns.psiElement(XmlAttributeValue.class)
            .withParent(PlatformPatterns.psiElement(XmlAttribute.class)
                    .withName("test")
                    .withParent(PlatformPatterns.psiElement(XmlTag.class)
                            .withName(StandardPatterns.string().oneOf("if", "when", "foreach", "bind"))));

    public static final PsiElementPattern.Capture<XmlAttributeValue> COLLECTION_ATTRIBUTE_VALUE = PlatformPatterns.psiElement(XmlAttributeValue.class)
            .withParent(PlatformPatterns.psiElement(XmlAttribute.class)
                    .withName("collection", "item")
                    .withParent(PlatformPatterns.psiElement(XmlTag.class)
                            .withName("foreach")));

    public static final PsiElementPattern.Capture<XmlAttributeValue> BIND_ATTRIBUTE_VALUE = PlatformPatterns.psiElement(XmlAttributeValue.class)
            .withParent(PlatformPatterns.psiElement(XmlAttribute.class)
                    .withName("value")
                    .withParent(PlatformPatterns.psiElement(XmlTag.class)
                            .withName("bind")));

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (!MapperUtils.isElementWithinMybatisFile(context)) {
            return;
        }

        if (TEST_ATTRIBUTE_VALUE.accepts(context) || COLLECTION_ATTRIBUTE_VALUE.accepts(context) || BIND_ATTRIBUTE_VALUE.accepts(context)) {
            OgnlLanguageInjector.injectElementWithPrefixSuffix(registrar, (PsiLanguageInjectionHost) context);
        }
    }

    @Override
    public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return List.of(XmlAttributeValue.class);
    }

}

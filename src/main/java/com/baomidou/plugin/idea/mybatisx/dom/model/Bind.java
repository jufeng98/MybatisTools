package com.baomidou.plugin.idea.mybatisx.dom.model;

import com.baomidou.plugin.idea.mybatisx.dom.converter.BindNameConverter;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * The interface Bind.
 *
 * @author yanglin
 */
public interface Bind extends DomElement {

    @NotNull
    @Attribute("name")
    @Convert(BindNameConverter.class)
    GenericAttributeValue<PsiElement> getName();

}

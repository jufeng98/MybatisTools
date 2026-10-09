package com.baomidou.plugin.idea.mybatisx.dom.model;

import com.baomidou.plugin.idea.mybatisx.dom.converter.AliasConverter;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The interface Parameter map.
 *
 * @author yanglin
 */
public interface ParameterMap extends IdDomElement {

    /**
     * Gets type.
     *
     * @return the type
     */
    @NotNull
    @Attribute("type")
    @Convert(AliasConverter.class)
    GenericAttributeValue<PsiClass> getType();

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    @SubTagList("parameter")
    List<Parameter> getParameters();

}

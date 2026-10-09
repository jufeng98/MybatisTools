package com.baomidou.plugin.idea.mybatisx.dom.model;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * The interface Package.
 *
 * @author yanglin
 */
public interface Package extends DomElement {

    /**
     * Gets name.
     *
     * @return the name
     */
    @NotNull
    @Attribute("name")
    GenericAttributeValue<String> getName();

}

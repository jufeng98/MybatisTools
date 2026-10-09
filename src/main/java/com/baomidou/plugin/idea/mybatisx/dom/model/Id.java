package com.baomidou.plugin.idea.mybatisx.dom.model;

import com.baomidou.plugin.idea.mybatisx.dom.converter.PropertyConverter;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.xml.*;

/**
 * The interface Id.
 *
 * @author yanglin
 */
public interface Id extends PropertyGroup {

    @Override
    @Attribute("property")
    @Convert(PropertyConverter.class)
    GenericAttributeValue<XmlAttributeValue> getProperty();

}

package com.baomidou.plugin.idea.mybatisx.dom.model;

import com.baomidou.plugin.idea.mybatisx.dom.converter.*;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.xml.*;

/**
 * The interface Property group.
 *
 * @author yanglin
 */
public interface PropertyGroup extends DomElement {

    /**
     * Gets property.
     *
     * @return the property
     */
    @Attribute("property")
    @Convert(PropertyConverter.class)
    GenericAttributeValue<XmlAttributeValue> getProperty();

    /**
     * column
     */
    @Attribute("column")
    @Convert(value = ColumnConverter.class, soft = true)
    GenericAttributeValue<XmlAttributeValue> getColumn();

    /**
     * jdbcType
     */
    @Attribute("jdbcType")
    @Convert(JdbcTypeConverter.class)
    GenericAttributeValue<XmlAttributeValue> getJdbcType();

    /**
     * jdbcType
     */
    @Attribute("typeHandler")
    @Convert(TypeHandlerConverter.class)
    GenericAttributeValue<PsiClass> getTypeHandler();
}

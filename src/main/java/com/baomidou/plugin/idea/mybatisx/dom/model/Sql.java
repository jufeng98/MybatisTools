package com.baomidou.plugin.idea.mybatisx.dom.model;

import com.baomidou.plugin.idea.mybatisx.dom.converter.SqlIdConverter;
import com.intellij.util.xml.*;

/**
 * The interface Sql.
 *
 * @author yanglin
 */
public interface Sql extends GroupOne, IdDomElement {

    @Attribute("id")
    @Convert(SqlIdConverter.class)
    GenericAttributeValue<Object> getId();


}

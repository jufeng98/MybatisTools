package com.baomidou.plugin.idea.mybatisx.dom.model;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The interface Configuration.
 *
 * @author yanglin
 */
@Namespace("MybatisConfiguration")
public interface Configuration extends DomElement {

    /**
     * Gets type aliases.
     *
     * @return the type aliases
     */
    @NotNull
    @SubTagList("typeAliases")
    List<TypeAliases> getTypeAliases();

}

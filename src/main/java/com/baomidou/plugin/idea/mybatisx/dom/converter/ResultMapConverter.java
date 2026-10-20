package com.baomidou.plugin.idea.mybatisx.dom.converter;

import com.baomidou.plugin.idea.mybatisx.dom.model.*;
import com.baomidou.plugin.idea.mybatisx.util.MapperUtils;
import com.google.common.collect.Maps;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Collection;

/**
 * The type Result map converter.
 *
 * @author yanglin
 */
public class ResultMapConverter extends IdBasedTagConverter {

    @NotNull
    @Override
    public Map<String, Collection<? extends IdDomElement>> getComparisons(@Nullable Mapper mapper) {
        if (mapper == null) {
            return Collections.emptyMap();
        }

        Map<String, Collection<? extends IdDomElement>> map = Maps.newHashMap();

        String namespace = MapperUtils.getNamespace(mapper);

        List<ResultMap> resultMaps = mapper.getResultMaps();

        map.put(namespace, resultMaps);
        return map;
    }

    @Override
    public boolean isContextResultMapSelf(IdDomElement domElement, ConvertContext context) {
        DomElement invocationElement = context.getInvocationElement();
        DomElement parent = invocationElement.getParent();

        boolean equals = domElement == parent;
        if (equals) {
            return true;
        }

        if (!(parent instanceof ResultMap resultMap)) {
            return false;
        }

        return Objects.equals(domElement.getId().getStringValue(), resultMap.getId().getStringValue());
    }

}
